package de.testbefund.testbefundapi.testing.data;

//TODO: Consider renaming to SampleStatus
//TODO: Add IGNORED state
public enum SampleStatus {
    ISSUED(false),
    CONFIRM_POSITIVE(true),
    CONFIRM_NEGATIVE(true);

    private final boolean isAffectedByGracePeriod;

    SampleStatus(boolean isAffectedByGracePeriod) {
        this.isAffectedByGracePeriod = isAffectedByGracePeriod;
    }

    public boolean isAffectedByGracePeriod() {
        return isAffectedByGracePeriod;
    }
}
