package de.testbefund.testbefundapi.test.data;

public enum TestStageStatus {
    ISSUED(false),
    CONFIRM_POSITIVE(true),
    CONFIRM_NEGATIVE(true);

    private final boolean hideable;

    TestStageStatus(boolean hideable) {
        this.hideable = hideable;
    }

    public boolean isHideable() {
        return hideable;
    }
}
