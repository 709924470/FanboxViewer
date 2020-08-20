package cn.settile.fanboxviewer.Network.RESTfulClient;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface FanboxAPI {
    // User functions
    @GET("user.countUnreadMessages")
    Call<ResponseBody> gerUnreadMessages();

    // Creator functions
    @GET("creator.listRecommended")
    Call<ResponseBody> getRecommendedCreators();

    @GET("creator.listFollowing")
    Call<ResponseBody> getFollowingCreators();

    @GET("creator.get")
    Call<ResponseBody> getCreatorInfo(@Query("creatorId") String creatorId);

    @GET("tag.getFeatured")
    Call<ResponseBody> getCreatorTags(@Query("creatorId") String creatorId);

    // Post functions
    @GET("post.listHome?limit={limit}")
    Call<ResponseBody> getHomePostList(@Query("limit") int limit);

    @GET("post.listSupporting?limit={limit}")
    Call<ResponseBody> getSupportingPostList(@Query("limit") int limit);

    @GET("post.info")
    Call<ResponseBody> getPostInfo(@Query("postId") int postId);

    @GET("post.listCreator?limit=10")
    Call<ResponseBody> getCreatorPosts(@Query("creatorId") String creatorId);

    /**
     *  userId: from @getCreatorInfo -> body.user.userId
     */
    @GET("post.listTagged")
    Call<ResponseBody> getCreatorPosts(@Query("tag") String tag, @Query("userId") int userId);

    // Plan functions
    @GET("plan.listSupporting")
    Call<ResponseBody> getSupportingPlans();

    @GET("plan.listCreator")
    Call<ResponseBody> getCreatorPlans(@Query("creatorId") String creatorId);

    // Notification/Bell functions
    @GET("bell.countUnread")
    Call<ResponseBody> getUnreadNotifications();

    @GET("bell.list?skipConvertUnreadNotification=0&commentOnly=0")
    Call<ResponseBody> getNotificationList(@Query("page") int page);

    @GET("bell.list?skipConvertUnreadNotification=0&commentOnly=1")
    Call<ResponseBody> getCommentList(@Query("page") int page);

    @GET("notification.getSettings")
    Call<ResponseBody> getNotificationSettings();

    @POST("notification.updateSettings")
    Call<ResponseBody> postUpdatedNotificationSettings(@Body String json);
}
