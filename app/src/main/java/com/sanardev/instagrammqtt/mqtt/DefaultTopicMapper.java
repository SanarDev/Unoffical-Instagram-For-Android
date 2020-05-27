package com.sanardev.instagrammqtt.mqtt;

import java.util.HashMap;
import java.util.Map;

public class DefaultTopicMapper implements TopicMapper {

    /**
     * @var int[]
     */
    private Map<String, Integer> map;

    /**
     * @var string[]
     */
    private Map<Integer, String> reversed;

    /**
     * @var LoggerInterface
     */
    private LoggerInterface logger;

    public DefaultTopicMapper(HashMap<String, Integer> map, LoggerInterface loggerInterface) {
        this.map = map;

        Map<Integer,String> myNewHashMap = new HashMap<>();
        for(Map.Entry<String, Integer> entry : map.entrySet()){
            myNewHashMap.put(entry.getValue(), entry.getKey());
        }
        this.reversed = myNewHashMap;
    }

    @Override
    public String map(String topic) {
        if (!map.containsKey(topic)) {
            this.logger.debug("Unknown topic " + topic);
            return topic;
        }
        String id = String.valueOf(map.get(topic));
        this.logger.debug("Topic " + topic + " has been mapped to " + id + ".");
        return id;
    }

    @Override
    public String unmap(String id) {
        if(!id.isEmpty() && id.startsWith("/")){
            return id;
        }
        if(!reversed.containsKey(id)){
            return id;
        }
        String topic = reversed.get((id));


        return topic;
    }
}
