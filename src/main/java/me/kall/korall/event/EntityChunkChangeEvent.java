package me.kall.korall.event;

import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.EntityEvent;

public class EntityChunkChangeEvent extends EntityEvent {
    public EntityChunkChangeEvent(Entity entity) {
        super(entity);
    }

    public static final class Pre extends EntityChunkChangeEvent {
        public Pre(Entity entity) {
            super(entity);
        }
    }

    public static final class Post extends EntityChunkChangeEvent {
        public Post(Entity entity) {
            super(entity);
        }
    }
}
