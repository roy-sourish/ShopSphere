package com.shopsphere.common.error;

import com.shopsphere.cart.exception.CartItemNotFoundException;
import com.shopsphere.cart.exception.CartNotFoundException;
import com.shopsphere.common.exception.OptimisticConflictException;
import com.shopsphere.inventory.exception.ReservationExpiredException;
import com.shopsphere.inventory.exception.ReservationNotFoundException;
import com.shopsphere.order.exception.EmptyCartException;
import com.shopsphere.order.exception.NoActiveCartException;
import com.shopsphere.order.exception.NoCheckedOutCartException;
import com.shopsphere.order.exception.OrderNotFoundException;
import com.shopsphere.product.exception.DuplicateProductException;
import com.shopsphere.product.exception.ProductNotFoundException;
import com.shopsphere.user.exception.DuplicateUserException;
import com.shopsphere.user.exception.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<FieldErrorResponse> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error ->
                        new FieldErrorResponse(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                ).collect(Collectors.toList());

        ErrorResponse response = new ErrorResponse(
                "VALIDATION_FAILED",
                "Request validation failed",
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex){
        ErrorResponse response = new ErrorResponse(
                "INVALID_PATH_VARIABLE",
                "Invalid path variable type",
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingFailure(){
        ErrorResponse response = new ErrorResponse(
                "CONCURRENT_MODIFICATION",
                "The resource was modified by another request. Reload and retry.",
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(OptimisticConflictException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticConflict(OptimisticConflictException ex) {

        ErrorResponse response = new ErrorResponse(
                "CONCURRENT_MODIFICATION",
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex){
        ErrorResponse response = new ErrorResponse(
                "VALIDATION_FAILED",
                "Request parameter validation failed",
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    /*
     USER EXCEPTIONS
     --------------------------------------------------------------------------------------
    */
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateUser(DuplicateUserException ex){
        ErrorResponse response = new ErrorResponse(
                "DUPLICATE_RESOURCE",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex){
        ErrorResponse response = new ErrorResponse(
                "USER_NOT_FOUND",
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /*
     PRODUCT EXCEPTIONS
     --------------------------------------------------------------------------------------
    */

    @ExceptionHandler(DuplicateProductException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateProduct(DuplicateProductException ex){
        ErrorResponse response = new ErrorResponse(
                "DUPLICATE_PRODUCT",
                ex.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex){
        ErrorResponse response = new ErrorResponse(
                "PRODUCT_NOT_FOUND",
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

      /*
     Cart EXCEPTIONS
     --------------------------------------------------------------------------------------
    */

    @ExceptionHandler(CartNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCartNotFound(CartNotFoundException ex){
        ErrorResponse response = new ErrorResponse(
                "CART_NOT_FOUND",
                 ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCartItemNotFound(CartItemNotFoundException ex){
        ErrorResponse response = new ErrorResponse(
                "CART_ITEM_NOT_FOUND",
                 ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }


    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {

        ErrorResponse response = new ErrorResponse(
                "INVALID_STATE",
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {

        ErrorResponse response = new ErrorResponse(
                "MISSING_HEADER",
                "Required header is missing: " + ex.getHeaderName(),
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ex.printStackTrace();
        ErrorResponse response = new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /*
 ORDER EXCEPTIONS
 --------------------------------------------------------------------------------------
*/

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex){
        ErrorResponse response = new ErrorResponse(
                "ORDER_NOT_FOUND",
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(NoActiveCartException.class)
    public ResponseEntity<ErrorResponse> handleNoActiveCart(NoActiveCartException ex){
        ErrorResponse response = new ErrorResponse(
                "NO_ACTIVE_CART",
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(NoCheckedOutCartException.class)
    public ResponseEntity<ErrorResponse> handleNoCheckedOutCart(NoCheckedOutCartException ex) {
        ErrorResponse response = new ErrorResponse(
                "NO_CHECKED_OUT_CART",
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<ErrorResponse> handleEmptyCart(EmptyCartException ex){
        ErrorResponse response = new ErrorResponse(
                "EMPTY_CART",
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReservationNotFound(ReservationNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(
                "RESERVATION_NOT_FOUND",
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ReservationExpiredException.class)
    public ResponseEntity<ErrorResponse> handleReservationExpired(ReservationExpiredException ex) {
        ErrorResponse response = new ErrorResponse(
                "RESERVATION_EXPIRED",
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
