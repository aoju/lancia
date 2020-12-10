package org.aoju.lancia.socket;

/**
 * Exception which indicates that the frame is not yet complete
 */
public class IncompleteException extends Exception {

    /**
     * It's Serializable.
     */
    private static final long serialVersionUID = 7330519489840500997L;

    /**
     * The preferred size
     */
    private final int preferredSize;

    /**
     * Constructor for the preferred size of a frame
     *
     * @param preferredSize the preferred size of a frame
     */
    public IncompleteException(int preferredSize) {
        this.preferredSize = preferredSize;
    }

    /**
     * Getter for the preferredSize
     *
     * @return the value of the preferred size
     */
    public int getPreferredSize() {
        return preferredSize;
    }

}
