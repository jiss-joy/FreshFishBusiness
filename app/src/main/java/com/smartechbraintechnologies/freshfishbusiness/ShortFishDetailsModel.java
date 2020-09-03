package com.smartechbraintechnologies.freshfishbusiness;

public class ShortFishDetailsModel {

    private String fishID;
    private String fishImage;
    private String fishName;
    private String fishPrice;
    private String fishAvailability;
    private String fishLocation;

    public ShortFishDetailsModel() {
    }

    public ShortFishDetailsModel(String fishID, String fishImage, String fishName, String fishPrice, String fishAvailability, String fishLocation) {
        this.fishID = fishID;
        this.fishImage = fishImage;
        this.fishName = fishName;
        this.fishPrice = fishPrice;
        this.fishAvailability = fishAvailability;
        this.fishLocation = fishLocation;
    }

    public String getFishID() {
        return fishID;
    }

    public String getFishImage() {
        return fishImage;
    }

    public String getFishName() {
        return fishName;
    }

    public String getFishPrice() {
        return fishPrice;
    }

    public String getFishAvailability() {
        return fishAvailability;
    }

    public String getFishLocation() {
        return fishLocation;
    }
}
