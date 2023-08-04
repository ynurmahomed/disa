package org.openmrs.module.disa.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class SyncLogTest {
    @Test
    public void equalsShouldBeTrueWhenObjectsAreEqual() {
        // Arrange
        String requestId = "request123";
        SyncLog log1 = new SyncLog(requestId, TypeOfResult.HIVVL);
        SyncLog log2 = new SyncLog(requestId, TypeOfResult.HIVVL);

        // Act & Assert
        assertThat(log1, equalTo(log2));
        assertThat(log2, equalTo(log1));
        assertThat(log1.hashCode(), equalTo(log2.hashCode()));
    }

    @Test
    public void equalsShouldBeFalseWhenObjectsAreNotEqualWithDifferentRequestId() {
        // Arrange
        SyncLog log1 = new SyncLog("request123", TypeOfResult.HIVVL);
        SyncLog log2 = new SyncLog("request456", TypeOfResult.HIVVL);

        // Act & Assert
        assertThat(log1, not(equalTo(log2)));
        assertThat(log2, not(equalTo(log1)));
    }

    @Test
    public void equalsShouldBeFalseWhenObjectsAreNotEqualWithDifferentTypeOfResult() {
        // Arrange
        SyncLog log1 = new SyncLog("request123", TypeOfResult.HIVVL);
        SyncLog log2 = new SyncLog("request123", TypeOfResult.CD4);

        // Act & Assert
        assertThat(log1, not(equalTo(log2)));
        assertThat(log2, not(equalTo(log1)));
    }

    @Test
    public void equalsShouldBeFalseWhenObjectsAreNotEqualWithNullRequestId() {
        // Arrange
        SyncLog log1 = new SyncLog(null, TypeOfResult.HIVVL);
        SyncLog log2 = new SyncLog("request123", TypeOfResult.HIVVL);

        // Act & Assert
        assertThat(log1, not(equalTo(log2)));
        assertThat(log2, not(equalTo(log1)));
    }

    @Test
    public void equalsShouldBeFalseWhenObjectsAreNotEqualWithNullTypeOfResult() {
        // Arrange
        SyncLog log1 = new SyncLog("request123", TypeOfResult.HIVVL);
        SyncLog log2 = new SyncLog("request123", null);

        // Act & Assert
        assertThat(log1, not(equalTo(log2)));
        assertThat(log2, not(equalTo(log1)));
    }

    @Test
    public void equalsShouldBeFalseWhenObjectsAreNotEqualWithDifferentClasses() {
        // Arrange
        SyncLog log = new SyncLog("request123", TypeOfResult.HIVVL);
        String someOtherObject = "Not a SyncLog object";

        // Act & Assert
        assertThat(log, not(equalTo(someOtherObject)));
    }
}
