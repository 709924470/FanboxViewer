package cn.settile.fanboxviewer.Network.RESTfulClient;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.PriorityQueue;

public class CachedQueue {
    private static HashMap<Integer, JSONObject> postCache = new HashMap<>();
    private static PriorityQueue<Integer> posts = new PriorityQueue<>();
    public static int MAX_POST_CACHE = 30;

    public static JSONObject getPostCache(int postId){
        if (postCache.containsKey(postId)){
            posts.remove(postId);
            posts.add(postId);
            return postCache.get(postId);
        }
        return null;
    }

    public static void addPostCache(int postId, JSONObject json){
        if (!postCache.containsKey(postId)){
            postCache.put(postId, json);
            posts.add(postId);
            if(posts.size() > MAX_POST_CACHE)
                postCache.remove(posts.poll());
        }
    }
}
