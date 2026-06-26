package nvt.vn.ute_forum.model.decorator;

import nvt.vn.ute_forum.dto.ForumPostDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Decorator Pattern — Base Decorator.
 *
 * Bọc ForumPostDTO để thêm thông tin bổ sung (badge, label...)
 * mà không thay đổi class gốc.
 *
 * Cách dùng:
 *   ForumPostDTO post = ...;
 *   post = new HotBadgeDecorator(post);   // thêm badge "🔥 Hot"
 *   post = new TrendingBadgeDecorator(post); // thêm badge "💬 Trending"
 *
 * Vì ForumPostDTO không phải interface nên Decorator kế thừa trực tiếp
 * và delegate toàn bộ getter/setter về wrapped object.
 */
public abstract class ForumPostDecorator extends ForumPostDTO {

    protected final ForumPostDTO wrapped;

    protected ForumPostDecorator(ForumPostDTO wrapped) {
        this.wrapped = wrapped;
    }

    // ====== Delegate toàn bộ getter/setter → wrapped ======

    @Override public String getId()                    { return wrapped.getId(); }
    @Override public void   setId(String id)           { wrapped.setId(id); }

    @Override public String getSubject()               { return wrapped.getSubject(); }
    @Override public void   setSubject(String s)       { wrapped.setSubject(s); }

    @Override public String getDescription()           { return wrapped.getDescription(); }
    @Override public void   setDescription(String d)   { wrapped.setDescription(d); }

    @Override public String getStatus()                { return wrapped.getStatus(); }
    @Override public void   setStatus(String s)        { wrapped.setStatus(s); }

    @Override public LocalDateTime getDate()           { return wrapped.getDate(); }
    @Override public void   setDate(LocalDateTime d)   { wrapped.setDate(d); }

    @Override public String getDepartmentName()        { return wrapped.getDepartmentName(); }
    @Override public void   setDepartmentName(String n){ wrapped.setDepartmentName(n); }

    @Override public String getUserName()              { return wrapped.getUserName(); }
    @Override public void   setUserName(String u)      { wrapped.setUserName(u); }

    @Override public List<String> getCategories()      { return wrapped.getCategories(); }
    @Override public void   setCategories(List<String> c){ wrapped.setCategories(c); }

    @Override public long getCommentCount()            { return wrapped.getCommentCount(); }
    @Override public void setCommentCount(long c)      { wrapped.setCommentCount(c); }

    @Override public String getReactionType()          { return wrapped.getReactionType(); }
    @Override public void   setReactionType(String r)  { wrapped.setReactionType(r); }

    @Override public String getReactionTypeLower()     { return wrapped.getReactionTypeLower(); }
    @Override public void   setReactionTypeLower(String r){ wrapped.setReactionTypeLower(r); }

    @Override public Map<String, Long> getReactions()  { return wrapped.getReactions(); }
    @Override public void   setReactions(Map<String, Long> r){ wrapped.setReactions(r); }

    @Override public long getTotalReactions()          { return wrapped.getTotalReactions(); }
    @Override public void setTotalReactions(long t)    { wrapped.setTotalReactions(t); }

    @Override public List<AttachmentDTO> getAttachments()       { return wrapped.getAttachments(); }
    @Override public void setAttachments(List<AttachmentDTO> a) { wrapped.setAttachments(a); }

    @Override public List<nvt.vn.ute_forum.dto.CommentDTO> getComments()       { return wrapped.getComments(); }
    @Override public void setComments(List<nvt.vn.ute_forum.dto.CommentDTO> c) { wrapped.setComments(c); }

    @Override public List<String> getTopReactionIcons() { return wrapped.getTopReactionIcons(); }
}