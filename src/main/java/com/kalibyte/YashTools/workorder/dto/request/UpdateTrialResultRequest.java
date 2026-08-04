package com.kalibyte.YashTools.workorder.dto.request;

import com.kalibyte.YashTools.workorder.entity.enums.TrialStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTrialResultRequest {

    @NotNull(message = "Trial status is required (SUCCESS or FAILED)")
    private TrialStatus status;

    private String feedback;
}
