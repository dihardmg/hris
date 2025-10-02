package hris.hris.dto;

import lombok.Data;
import java.util.List;

@Data
public class PaginatedLeaveRequestResponse {
    private List<LeaveRequestResponseDto> data;
    private PageInfo page;

    @Data
    public static class PageInfo {
        private int size;
        private long total;
        private int totalPages;
        private int current;
    }

    public static PaginatedLeaveRequestResponse createResponse(List<LeaveRequestResponseDto> data,
                                                               int size, long total, int totalPages, int current) {
        PaginatedLeaveRequestResponse response = new PaginatedLeaveRequestResponse();
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