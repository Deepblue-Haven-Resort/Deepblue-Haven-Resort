package deepbluehaven.dto;

public class AmenityViewDTO {
    private String icon;
    private String label;
 
    public AmenityViewDTO(String icon, String label) {
        this.icon = icon;
        this.label = label;
    }
 
    public String getIcon() { 
        return icon; 
    }
    
    public String getLabel() { 
        return label; 
    }
}
