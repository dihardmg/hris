package hris.hris.dto;

import lombok.Data;
import java.util.List;

@Data
public class PaginatedAttendanceResponse {
    private List<AttendanceDto> data;
    private PageInfo page;

    @Data
    public static class PageInfo {
        private int size;
        private long total;
        private int totalPages;
        private int current;
    }

    public static PaginatedAttendanceResponse createResponse(List<AttendanceDto> data,
                                                            int size, long total, int totalPages, int current) {
        PaginatedAttendanceResponse response = new PaginatedAttendanceResponse();
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