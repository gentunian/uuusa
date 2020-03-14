package org.grupolys.spring.validations;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = ExistsIn.Validator.class)
@Documented
public @interface ExistsIn {

    String message() default "Required data does not exists.";
    String collection();
    String database();
    String field() default "name";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};


    class Validator implements ConstraintValidator<ExistsIn, String> {

        @Autowired
        private MongoClient mongoClient;
        private String collectionName;
        private String databaseName;
        private String fieldName;

        @Override
        public void initialize(ExistsIn constraintAnnotation) {
            this.collectionName = constraintAnnotation.collection();
            this.databaseName = constraintAnnotation.database();
            this.fieldName = constraintAnnotation.field();
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            MongoDatabase db = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = db.getCollection(collectionName);
            String[] selector = fieldName.split(".");
            if (selector.length == 1) {
                Filters.eq(fieldName, value);
            } else {

            }
            Document doc = collection.find(Filters.eq(fieldName, value)).first();
            return doc != null;
        }
    }
}