package cn.settile.fanboxviewer.RESTfulClient;

import retrofit2.Call;
import retrofit2.http.*;

public interface FanboxAPI {
    // User functions
    @GET("user.countUnreadMessages")
    Call<String> gerUnreadMessages();

    // Creator functions
    @GET("creator.listRecommended")
    Call<String> getRecommendedCreators();

    @GET("creator.listFollowing")
    Call<String> getFollowingCreators();

    @GET("creator.get")
    Call<String> getCreatorInfo(@Query("creatorId") String creatorId);

    @GET("tag.getFeatured")
    Call<String> getCreatorTags(@Query("creatorId") String creatorId);

    // Post functions
    @GET("post.listHome?limit={limit}")
    Call<String> getHomePostList(@Query("limit") int limit);

    @GET("post.listSupporting?limit={limit}")
    Call<String> getSupportingPostList(@Query("limit") int limit);

    @GET("post.info")
    Call<String> getPostInfo(@Query("postId") int postId);

    @GET("post.listCreator?limit=10")
    Call<String> getCreatorPosts(@Query("creatorId") String creatorId);

    /**
     *  userId: from @getCreatorInfo -> body.user.userId
     */
    @GET("post.listTagged")
    Call<String> getCreatorPosts(@Query("tag") String tag, @Query("userId") int userId);

    // Plan functions
    @GET("plan.listSupporting")
    Call<String> getSupportingPlans();

    @GET("plan.listCreator")
    Call<String> getCreatorPlans(@Query("creatorId") String creatorId);

    // Notification/Bell functions
    @GET("bell.countUnread")
    Call<String> getUnreadNotifications();

    @GET("bell.list?skipConvertUnreadNotification=0&commentOnly=0")
    Call<String> getNotificationList(@Query("page") int page);

    @GET("bell.list?skipConvertUnreadNotification=0&commentOnly=1")
    Call<String> getCommentList(@Query("page") int page);

    @GET("notification.getSettings")
    Call<String> getNotificationSettings();

    @POST("notification.updateSettings")
    Call<String> postUpdatedNotificationSettings(@Body String json);
}
