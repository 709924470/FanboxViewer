package cn.settile.fanboxviewer.RESTfulClient;

public class FanboxParser {
    public FanboxParser(FanboxAPI client, String user) throws Exception {
        this.creator = new CreatorBean()
                .setUser(user);
        this.client = client;

        Call<String> creatorInfoCaller = client.getCreatorInfo(user);
        Response<String> creatorInfoResp= creatorInfoCaller.execute();

        assert Objects.isNull(creatorInfoResp.body()) || !creatorInfoResp.isSuccessful() || creatorInfoResp.code() != 200;

        JSONObject info = new JSONObject(creatorInfoResp.body());
        info = info.getJSONObject("body");

        List<ImageBean> images = new ArrayList<>();
        info.getJSONArray("profileItems").forEach((e) -> {
            ImageBean image = new ImageBean();
            image.url = ((JSONObject) e).getString("imageUrl");
            image.thumbUrl = ((JSONObject) e).getString("thumbnailUrl");
            images.add(image);
        });

        creator
                .setName(info.getJSONObject("user").getString("name"))
                .setCoverUrl(info.getString("coverImageUrl"))
                .setDesc(info.getString("description"))
                .setLinks(info.getJSONArray("profileLinks").toList())
                .setImages(images)
                .setIconUrl(info.getJSONObject("user").getString("iconUrl"))
                .setUserId(info.getJSONObject("user").getInt("userId"))
                .setFollowing(info.getBoolean("isFollowed"))
                .setSupporting(info.getBoolean("isSupported"));
    }
}
