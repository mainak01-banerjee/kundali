package tech.mainak.kundali.kundali_2.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Period {
    private String start;
    private String end;

    public Period() {}

    public Period(String start, String end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return start+"-"+ end;
    }


}
