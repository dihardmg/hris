package hris.hris.dto;

import lombok.Data;
import java.util.List;

@Data
public class PaginatedBusinessTravelRequestResponse {
    private List<BusinessTravelRequestResponseDto> data;
    private PageInfo page;

    public static PaginatedBusinessTravelRequestResponse createResponse(List<BusinessTravelRequestResponseDto> data,
                                                                      int size, long total, int totalPages, int current) {
        PaginatedBusinessTravelRequestResponse response = new PaginatedBusinessTravelRequestResponse();
        response.setData(data);

        PageInfo pageInfo = new PageInfo();
        pageInfo.setSize(size);
        pageInfo.setTotal(total);
        pageInfo.setTotalPages(totalPages);
        pageInfo.setCurrent(current);
        response.setPage(pageInfo);

        return response;
    }
}