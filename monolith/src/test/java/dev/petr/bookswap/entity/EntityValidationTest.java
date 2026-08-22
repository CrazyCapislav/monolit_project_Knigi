package dev.petr.bookswap.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EntityValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void userValidationSuccess() {
        User user = User.builder()
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .displayName("Test User")
                .role(Role.USER)
                .createdAt(OffsetDateTime.now())
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @Test
    void userValidationFailsWithInvalidEmail() {
        User user = User.builder()
                .email("invalid-email")
                .passwordHash("hashedPassword")
                .displayName("Test User")
                .role(Role.USER)
                .createdAt(OffsetDateTime.now())
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    void userValidationFailsWithBlankEmail() {
        User user = User.builder()
                .email("")
                .passwordHash("hashedPassword")
                .displayName("Test User")
                .role(Role.USER)
                .createdAt(OffsetDateTime.now())
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void bookValidationSuccess() {
        User owner = User.builder().id(1L).email("owner@test.com")
                .passwordHash("hash").displayName("Owner")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build();

        Book book = Book.builder()
                .title("Valid Book Title")
                .author("Valid Author")
                .isbn("123-456")
                .publishedYear(2020)
                .owner(owner)
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now())
                .build();

        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).isEmpty();
    }

    @Test
    void bookValidationFailsWithBlankTitle() {
        User owner = User.builder().id(1L).email("owner@test.com")
                .passwordHash("hash").displayName("Owner")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build();

        Book book = Book.builder()
                .title("")
                .author("Valid Author")
                .owner(owner)
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now())
                .build();

        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }

    @Test
    void bookValidationFailsWithInvalidYear() {
        User owner = User.builder().id(1L).email("owner@test.com")
                .passwordHash("hash").displayName("Owner")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build();

        Book book = Book.builder()
                .title("Title")
                .author("Author")
                .publishedYear(3000)
                .owner(owner)
                .status(BookStatus.AVAILABLE)
                .condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now())
                .build();

        Set<ConstraintViolation<Book>> violations = validator.validate(book);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("publishedYear"));
    }

    @Test
    void genreValidationSuccess() {
        Genre genre = Genre.builder()
                .name("Science Fiction")
                .build();

        Set<ConstraintViolation<Genre>> violations = validator.validate(genre);
        assertThat(violations).isEmpty();
    }

    @Test
    void genreValidationFailsWithBlankName() {
        Genre genre = Genre.builder()
                .name("")
                .build();

        Set<ConstraintViolation<Genre>> violations = validator.validate(genre);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void bookRatingValidationSuccess() {
        User user = User.builder().id(1L).email("user@test.com")
                .passwordHash("hash").displayName("User")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build();

        User owner = User.builder().id(2L).email("owner@test.com")
                .passwordHash("hash").displayName("Owner")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build();

        Book book = Book.builder().id(1L).title("Book").author("Author")
                .owner(owner).status(BookStatus.AVAILABLE).condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now()).build();

        BookRating rating = BookRating.builder()
                .user(user)
                .book(book)
                .rating(4)
                .comment("Good book")
                .ratedAt(OffsetDateTime.now())
                .build();

        Set<ConstraintViolation<BookRating>> violations = validator.validate(rating);
        assertThat(violations).isEmpty();
    }

    @Test
    void bookRatingValidationFailsWithInvalidRating() {
        User user = User.builder().id(1L).email("user@test.com")
                .passwordHash("hash").displayName("User")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build();

        User owner = User.builder().id(2L).email("owner@test.com")
                .passwordHash("hash").displayName("Owner")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build();

        Book book = Book.builder().id(1L).title("Book").author("Author")
                .owner(owner).status(BookStatus.AVAILABLE).condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now()).build();

        BookRating rating = BookRating.builder()
                .user(user)
                .book(book)
                .rating(10)
                .ratedAt(OffsetDateTime.now())
                .build();

        Set<ConstraintViolation<BookRating>> violations = validator.validate(rating);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("rating"));
    }

    @Test
    void bookRatingValidationFailsWithZeroRating() {
        User user = User.builder().id(1L).email("user@test.com")
                .passwordHash("hash").displayName("User")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build();

        User owner = User.builder().id(2L).email("owner@test.com")
                .passwordHash("hash").displayName("Owner")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build();

        Book book = Book.builder().id(1L).title("Book").author("Author")
                .owner(owner).status(BookStatus.AVAILABLE).condition(BookCondition.GOOD)
                .createdAt(OffsetDateTime.now()).build();

        BookRating rating = BookRating.builder()
                .user(user)
                .book(book)
                .rating(0)
                .ratedAt(OffsetDateTime.now())
                .build();

        Set<ConstraintViolation<BookRating>> violations = validator.validate(rating);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void publicationRequestValidationSuccess() {
        User requester = User.builder().id(1L).email("requester@test.com")
                .passwordHash("hash").displayName("Requester")
                .role(Role.USER).createdAt(OffsetDateTime.now()).build();

        User publisher = User.builder().id(2L).email("publisher@test.com")
                .passwordHash("hash").displayName("Publisher")
                .role(Role.PUBLISHER).createdAt(OffsetDateTime.now()).build();

        PublicationRequest pr = PublicationRequest.builder()
                .requester(requester)
                .publisher(publisher)
                .title("New Book")
                .author("New Author")
                .message("Please publish this")
                .status(PublicationStatus.SUBMITTED)
                .createdAt(OffsetDateTime.now())
                .build();

        Set<ConstraintViolation<PublicationRequest>> violations = validator.validate(pr);
        assertThat(violations).isEmpty();
    }
}

