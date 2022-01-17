package com.smartdesk.model.SmartDesk;

import java.util.ArrayList;
import java.util.List;

public class DesksSortedList {
    public List<NewDesk> deskListNew = new ArrayList<>();
    public List<String> dateList = new ArrayList<>();

    public List<NewDesk> getDeskListNew() {
        return deskListNew;
    }

    public void setDeskListNew(List<NewDesk> deskListNew) {
        this.deskListNew = deskListNew;
    }

    public List<String> getDateList() {
        return dateList;
    }

    public void setDateList(List<String> dateList) {
        this.dateList = dateList;
    }

    public void clear() {
        this.dateList.clear();;
        this.deskListNew.clear();;
    }

    public void addAll(List<NewDesk> filterData,List<String> datesList) {
        this.deskListNew = filterData;
        this.dateList = datesList;
    }
}
