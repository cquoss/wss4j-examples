package de.quoss.wss4j.examples;

public class Wss4jExamplesException extends RuntimeException {

    public Wss4jExamplesException(final String s) {
        super(s);
    }

    public Wss4jExamplesException(final Throwable t) {
        super(t);
    }

    public Wss4jExamplesException(final String s, final Throwable t) {
        super(s, t);
    }

}
