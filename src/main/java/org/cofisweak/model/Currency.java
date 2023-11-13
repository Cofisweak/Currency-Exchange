package org.cofisweak.model;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Currency {
    int id;
    String code;
    @SerializedName("name")
    String fullName;
    String sign;
}
