package hris.hris.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityDropdownDto {
    private Long id;
    private String cityCode;
    private String cityName;
    private String provinceName;
    private String displayName; // Format: "CityName, ProvinceName"
}