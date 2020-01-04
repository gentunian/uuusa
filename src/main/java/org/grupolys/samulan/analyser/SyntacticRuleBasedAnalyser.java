package org.grupolys.samulan.analyser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import org.grupolys.samulan.analyser.operation.Operation;
import org.grupolys.samulan.rule.RulesManager;
import org.grupolys.samulan.util.*;
import org.grupolys.samulan.util.dictionary.Dictionary;

/**
 * Universal Unsupervised Uncovered Syntactic Analyser
 * @author David Vilares
 *
 */
public class SyntacticRuleBasedAnalyser extends RuleBasedAnalyser implements Analyser  {

	/**
	 *
 	 * @param ac analyser configuration
	 * @param rm rule manager
	 */
	public SyntacticRuleBasedAnalyser(AnalyserConfiguration ac, RulesManager rm){
		super(ac, rm);
	}

	/**
	 *
	 * @param node node
	 * @return returns semantic orientation as float value
	 */
	private float getSemanticOrientation(SentimentDependencyNode node) {
		Dictionary dict = rm.getDictionary();
		String cpostag = node.getCpostag();
		String word = node.getWord();
		String form = node.getLemma() != null? node.getLemma() : word;
		String dictLemma = dict.getLemma(cpostag, word);

		float dictionaryValue = dict.getValue(cpostag, dictLemma,false);
		
		if (dictionaryValue == 0 && !dict.getAdverbsIntensifiers().contains(form)){
			//TODO replaceAll should not be here
			String predictedFormNoDuplicated = dict.getLemma(cpostag, word.replaceAll("(.)\\1+", "$1"));
			dictionaryValue = dict.getValue(cpostag, predictedFormNoDuplicated, ac.isRelaxedEmotionSearch());
		}

		if (dictionaryValue == 0){
			String strippedLemma = dict.getStrippedLemma(cpostag, word);
			dictionaryValue = dict.getValue(cpostag, strippedLemma, ac.isRelaxedEmotionSearch());
		}

		node.setWordWeight(dictionaryValue);
		return dictionaryValue;
	}

	private void updateLevelsUp(List<QueuedOperationInformation> qois){
		for (QueuedOperationInformation qoi: qois){
			if (qoi.getLevelsUp() > 0) {
				qoi.setLevelsUp((short) (qoi.getLevelsUp() - 1));
			}
		}
	}

	private void queueNodeOperations(List<Operation> operations, SentimentInformation si, SentimentDependencyNode node){
		for (Operation o: operations){
			short levelToApply = o.getRule().getLevelsup();

			if (o.getOperationName().equals(Operation.DEFAULT)){
				si.setSemanticOrientation(getSemanticOrientation(node));
				int isPositiveSentiment = si.getSemanticOrientation() > 0 ? 1 : 0;
				int isNegativeSentiment = si.getSemanticOrientation() < 0 ? 1 : 0;
				si.setPositiveSentiment(
						Math.max(
								Math.abs(si.getSemanticOrientation()) * (isPositiveSentiment),
								SentimentInformation.SENTISTRENGTH_NEUTRAL
						)
				);
				si.setNegativeSentiment(
						Math.max(
								Math.abs(si.getSemanticOrientation()) * (isNegativeSentiment),
								SentimentInformation.SENTISTRENGTH_NEUTRAL
						)
				);

			}
			si.addQueueOperation(new QueuedOperationInformation(levelToApply, o));
		}
	}

	private boolean isPendingOperation(QueuedOperationInformation qoi) {
		return qoi.getLevelsUp() > 0;
	}

	private List<QueuedOperationInformation> getOperationsToQueue(List<QueuedOperationInformation> qois) {
		List <QueuedOperationInformation> pendingOperations = new ArrayList<>();
		for (QueuedOperationInformation qoi: qois){
			if (isPendingOperation(qoi)) {
				pendingOperations.add(qoi);
			}
		}
		return pendingOperations;
	}

	private PriorityQueue<QueuedOperationInformation> getOperationsToApply(List<QueuedOperationInformation> qois) {
		Comparator<QueuedOperationInformation> comparator = (qoi1, qoi2) ->
				qoi2.getOperation().getPriority() - qoi1.getOperation().getPriority();
		
		PriorityQueue <QueuedOperationInformation> queuedOperations =
				new PriorityQueue<>(qois.size() + 1, comparator);

		for (QueuedOperationInformation qoi: qois){
			if (!isPendingOperation(qoi)) {
				queuedOperations.add(qoi);
			}
		}

		return queuedOperations;
	}

	private List<QueuedOperationInformation> getAllQueuedOperations(SentimentInformation head,
																	List<SentimentInformation> children) {
		
		List<QueuedOperationInformation> allQueuedOperations = new ArrayList<>(head.getQueuedOperations());

		for (SentimentInformation siChild: children){
			for (QueuedOperationInformation oChild : siChild.getQueuedOperations()){
				//Nesting weighting operations
				//TODO only supports double nesting
				short headAddress = siChild.getSentimentDependencyNode().getHead();
				SentimentDependencyGraph sdgChild = siChild.getSentimentDependencyGraph();
				SentimentDependencyNode headNode = sdgChild.getNode(headAddress);
				String headLemma = rm.getDictionary()
						.getLemma(headNode.getCpostag(), headNode.getWord());
				SentimentDependencyNode grandPaNode = sdgChild.getNode(headNode.getHead());
				String grandPaLemma = rm.getDictionary()
						.getLemma(grandPaNode.getCpostag(), grandPaNode.getWord());
				boolean grandPaIsSubjective = rm.getDictionary()
						.getValue(grandPaNode.getCpostag(), grandPaLemma, true) != 0;

				if (rm.getDictionary().isWeight(headLemma) && grandPaIsSubjective) {
					oChild.setLevelsUp((short) (oChild.getLevelsUp()+1));
				}

				allQueuedOperations.add(oChild);
			}
		}

		return allQueuedOperations;
	}
	
	
	/**
	 * Given the SentimentInformation of a head term and its children, it computes the merged SentimentInformation 
	 * that results after computing all operations to be applied at that stage.
	 * @param head: SentimentInformation object corresponding to the head (as a single node).
	 * @param children: List of SentimentInformation objects corresponding to the computed/merged
	 *                  SentimentInformation rooted at each child of the head term
	 * @return A new SentimentInformation corresponding to the computed/merged SentimentInformation
	 * rooted at the head term
	 */
	public SentimentInformation calculate(SentimentInformation head,
										  List<SentimentInformation> children) {

		SentimentInformation newHead = new SentimentInformation(head);
		List<QueuedOperationInformation> allOperations = getAllQueuedOperations(newHead, children);
		List<QueuedOperationInformation> qOperations = getOperationsToQueue(allOperations);
		PriorityQueue<QueuedOperationInformation> aOperations = getOperationsToApply(allOperations);
		String appliedOperations = "";
		QueuedOperationInformation i;
		OperationValue ov;

		while ((i = aOperations.poll()) != null ) {
			ov = i.getOperation().apply(newHead, children);
			//Logging the applied operation at node i
			//TODO: Improve how we track.
			appliedOperations = appliedOperations.concat(ov.appliedOperation() == null ? "" : ov.appliedOperation()+",");
			newHead = ov.getHead();
			children = ov.getChildren();
		}

		ac.getSentimentJoiner().join(newHead, children);
		newHead.setSentimentInformationInGraph(); //newHead has the reference to its graph
		newHead.setOperationExplanation(appliedOperations);
		
		//We add q(eued)Operations comming from the children to the head, to spread them through the tree
		for (QueuedOperationInformation pd : qOperations) {
			if (!newHead.getQueuedOperations().contains(pd)) {
				newHead.getQueuedOperations().add(pd);
			}
		}

		List<QueuedOperationInformation> aux = new ArrayList<>();
		for (QueuedOperationInformation pd : newHead.getQueuedOperations()) {
			if (isPendingOperation(pd)) {
				aux.add(pd);
			}
		}

		newHead.setQueuedOperations(aux);
		updateLevelsUp(newHead.getQueuedOperations());

		return newHead;
	}
	
	/**
	 * It computes the SentimentInformation of sentence represented as a SentimentDependencyGraph
	 * @param dg: The sentence represented as a SentimentDependencyGraph
	 * @param address: (usually the dummy root, id=0)
	 * @return The SentimentInformation for the branch of dg rooted at address
	 */
	public SentimentInformation analyse(SentimentDependencyGraph dg, short address) {
		List<Operation> operations = rm.getOperations(dg, address);
		SentimentDependencyNode node = dg.getNode(address);
		SentimentInformation siHead = new SentimentInformation(0, node, dg, new ArrayList<>());

		//Queuing operations in node
		queueNodeOperations(operations, siHead, node);

		if (node.isLeaf()) {
			siHead = this.calculate(siHead, new ArrayList<>());
		} else {
			List<Short> children = node.getDependents();
			List<SentimentInformation> siChildren = new ArrayList<>();

			for (Short child: children) {
				SentimentInformation siChild = this.analyse(dg, child);
				siChildren.add(siChild);
			}

			siHead = this.calculate(siHead, siChildren);
		}

		return siHead;
	}


	/**
	 * It computes the final SentimentInformation of a sample, given a list of SentimentInformation objects.
	 * It is intended for running document-level sentiment classification.
	 * @param sis: One SentimentInformation object per sentence.
	 * @return A SentimentInformation object resulting for summing the SentimentInformation coming from each sentence.
	 */
	@Override
	public SentimentInformation merge(List<SentimentInformation> sis) {
		SentimentInformation si = new SentimentInformation();
		float posSentiment = 0;
		float negSentiment = 0;
		
		for (SentimentInformation siAux : sis){
			posSentiment += siAux.getPositiveSentiment();
			negSentiment += siAux.getNegativeSentiment();
		}
			
		si.setNegativeSentiment(negSentiment);
		si.setPositiveSentiment(posSentiment);
		si.setSemanticOrientation(posSentiment - negSentiment);

		return si;
	}
}
