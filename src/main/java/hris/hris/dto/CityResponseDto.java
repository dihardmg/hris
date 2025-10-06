package hris.hris.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CityResponseDto {
    private Long id;
    private String cityName;
    private String provinceName;
    private String cityDisplayName;
}