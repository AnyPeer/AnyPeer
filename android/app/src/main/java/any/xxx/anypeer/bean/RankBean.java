package any.xxx.anypeer.bean;

import java.io.Serializable;

public class RankBean implements Serializable {

    private String Producer_public_key;
    private String Value;
    private String Address;
    private String Rank;
    private String Ownerpublickey;
    private String Nodepublickey;
    private String Nickname;
    private String Url;
    private String Location;
    private int Active;
    private String Votes;
    private String Netaddress;
    private String State;
    private String Registerheight;
    private String Cancelheight;
    private String Inactiveheight;
    private String Illegalheight;
    private String Index;
    private String Reward;
    private String EstRewardPerYear;

    public String getProducer_public_key() {
        return Producer_public_key;
    }

    public void setProducer_public_key(String producer_public_key) {
        Producer_public_key = producer_public_key;
    }

    public String getValue() {
        return Value;
    }

    public void setValue(String value) {
        Value = value;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getRank() {
        return Rank;
    }

    public void setRank(String rank) {
        Rank = rank;
    }

    public String getOwnerpublickey() {
        return Ownerpublickey;
    }

    public void setOwnerpublickey(String ownerpublickey) {
        Ownerpublickey = ownerpublickey;
    }

    public String getNodepublickey() {
        return Nodepublickey;
    }

    public void setNodepublickey(String nodepublickey) {
        Nodepublickey = nodepublickey;
    }

    public String getNickname() {
        return Nickname;
    }

    public void setNickname(String nickname) {
        Nickname = nickname;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public int getActive() {
        return Active;
    }

    public void setActive(int active) {
        Active = active;
    }

    public String getVotes() {
        return Votes;
    }

    public void setVotes(String votes) {
        Votes = votes;
    }

    public String getNetaddress() {
        return Netaddress;
    }

    public void setNetaddress(String netaddress) {
        Netaddress = netaddress;
    }

    public boolean isValid() {
        return State.equals("Active");
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getRegisterheight() {
        return Registerheight;
    }

    public void setRegisterheight(String registerheight) {
        Registerheight = registerheight;
    }

    public String getCancelheight() {
        return Cancelheight;
    }

    public void setCancelheight(String cancelheight) {
        Cancelheight = cancelheight;
    }

    public String getInactiveheight() {
        return Inactiveheight;
    }

    public void setInactiveheight(String inactiveheight) {
        Inactiveheight = inactiveheight;
    }

    public String getIllegalheight() {
        return Illegalheight;
    }

    public void setIllegalheight(String illegalheight) {
        Illegalheight = illegalheight;
    }

    public String getIndex() {
        return Index;
    }

    public void setIndex(String index) {
        Index = index;
    }

    public String getReward() {
        return Reward;
    }

    public void setReward(String reward) {
        Reward = reward;
    }

    public String getEstRewardPerYear() {
        return EstRewardPerYear;
    }

    public void setEstRewardPerYear(String estRewardPerYear) {
        EstRewardPerYear = estRewardPerYear;
    }
}
