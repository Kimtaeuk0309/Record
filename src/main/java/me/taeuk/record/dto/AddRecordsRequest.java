package me.taeuk.record.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AddRecordsRequest {

    private List<AddRecordDataRequest> records;

}
