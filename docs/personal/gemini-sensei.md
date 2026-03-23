# Good Gemini Responses for Some Interesting Questions

## Model: Gemini 3 Pro

---

### 1. Why [UBLNamespaces](./../src/main/java/sa/abrahman/zaxeg/infrastructure/out/dto/UBLNamespaces.java) is an interface and not an enum?

In this specific case, **the Java compiler will literally stop you from using an enum.**

The constants we are defining are being used inside Jackson annotations, like this:
`@JacksonXmlProperty(namespace = UblNamespaces.CAC)`

In Java, the values passed into an annotation parameter **must be compile-time constant expressions** (specifically, primitives or `String` literals).

If you created an enum like this:

```java
public enum UblNamespaces {
    CAC("urn:...:CommonAggregateComponents-2");
    private final String value;
    // constructor and getter...
}

```

And then tried to use it in the annotation:
`@JacksonXmlProperty(namespace = UblNamespaces.CAC.getValue())`

**The Java compiler will throw a fatal error:** _"Attribute value must be constant."_ Method calls (like `.getValue()`) are evaluated at runtime, not compile time.

By using an `interface`, every variable declared inside it is implicitly `public static final`—meaning it is a perfect compile-time string literal that the annotations will accept. (Note: A `public final class` with a private constructor and static strings is also an acceptable, slightly stricter alternative to an interface).
