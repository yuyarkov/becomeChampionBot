import pojo.Dancer;

public class Pair extends pojo.Dancer {
    private pojo.Dancer leader;
    private pojo.Dancer follower;

    public Dancer getLeader() {
        return leader;
    }
    public void setLeader(Dancer leader) {
        this.leader = leader;
    }

    public Dancer getFollower() {
        return follower;
    }
    public void setFollower(Dancer follower) {
        this.follower = follower;
    }


    public Pair (pojo.Dancer dancer1, pojo.Dancer dancer2) {
        if (dancer1.getSex().equals(Dancer.LEADER)) {
            this.leader=dancer1;
            this.follower=dancer2;
        }
        else {
            this.leader=dancer2;
            this.follower=dancer1;
        }
    }

    public Dancer getOtherDancerInPair(Dancer dancer1) {
        if (this.leader.equals(dancer1)) {return this.follower;}
        else {return this.leader;}
    }

}
