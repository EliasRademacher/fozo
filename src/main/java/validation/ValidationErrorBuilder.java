package validation;

import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;


/**
 * A simple helper to build ValidationError objects, as described on
 * http://blog.codeleak.pl/2013/09/request-body-validation-in-spring-mvc-3.2.html
 */
public class ValidationErrorBuilder {

    public static ValidationError fromBindingErrors(Errors errors) {
        ValidationError error = new ValidationError("Validation failed. " + errors.getErrorCount() + " error(s)");
        for (ObjectError objectError : errors.getAllErrors()) {
            error.addValidationError(objectError.getDefaultMessage());
        }
        return error;
    }
}