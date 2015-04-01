package io.github.arven.rs.hypertext;

import com.google.common.collect.Lists;
import io.github.arven.rs.services.example.Group;
import io.github.arven.rs.services.example.Message;
import io.github.arven.rs.services.example.Person;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * The Hyper class is a wrapper for most data types, including lists, which
 * provides a convenient way to bootstrap the links in return values as well
 * as rewrite the lists. It supports injection into classes which implement
 * HyperlinkIdentifier, as well as gets its data from two annotations: the
 * HyperlinkPath class annotation, and the HyperlinkId method and field
 * annotation.
 * 
 * @author Brian Becker
 * @param <ResponseType>
 */
@XmlRootElement(name = "api")
@XmlAccessorType(XmlAccessType.NONE)
@XmlSeeAlso({Person.class, Group.class, Message.class})
public class Hyper<ResponseType> {
    
    @XmlAttribute   private Integer offset = null;
    @XmlAttribute   private Boolean reverse = false;
    @XmlAttribute   private Integer limit = null;
    private UriBuilder self = null;
    private List<String> eachActions = new LinkedList<String>();
    private String type = "application/xml";
    private Class<?> matcher = null;
    private String method = "";
    
    @XmlAttribute
    public Integer getSize() {
        if(this.content != null) {
            return this.content.size();
        } else {
            return 0;
        }
    }
    
    private final Collection<Link> links = new LinkedList<Link>();
    
    @XmlAnyElement
    private List<ResponseType> content;
    
    public Hyper() {
    }
    
    public static class Builder<ResponseType> {
        
        private final Hyper<ResponseType> response = new Hyper();
        
        public Builder() {
            
        }
        
        public Builder<ResponseType> entityList(List<ResponseType> entity) {
            response.content = entity;
            return this;
        }
        
        public Builder<ResponseType> entity(ResponseType entity) {
            response.content = Arrays.asList(entity);
            return this;
        }
        
        public Builder<ResponseType> type(String type) {
            response.type = type;
            return this;
        }

        public Builder<ResponseType> link(Link link) {
            if(link.getRels().contains("self")) {
                response.self = link.getUriBuilder();
            }
            response.links.add(link);
            return this;
        }

        public Builder<ResponseType> offset(Integer offset) {
            response.offset = offset;
            if(offset != null && offset < response.content.size()) {
                response.content = response.content.subList(offset, response.content.size());
            }
            return this;
        }

        public Builder<ResponseType> limit(Integer limit) {
            response.limit = limit;
            if(limit != null && limit > 0) {
                response.content = response.content.subList(0, Math.min(limit, response.content.size()));
            } else {
                response.content = Collections.emptyList();
            }
            return this;
        }   

        public Builder<ResponseType> reverse(Boolean reverse) {
            response.reverse = reverse;
            if(reverse != null && reverse == true) {
                response.content = Lists.reverse(response.content);
            }
            return this;
        }
        
        public Builder<ResponseType> each(String... action) {
            response.eachActions.addAll(Arrays.asList(action));
            return this;
        }
        
        public Builder<ResponseType> matcher(Class matched) {
            response.matcher = matched;
            return this;
        }
        
        public Builder<ResponseType> method(String method) {
            response.method = method;
            return this;
        }
        
        public Hyper<ResponseType> build() {
            if(response.self != null) {
                for(Object o : response.content) {
                    if(o.getClass().isAnnotationPresent(HyperlinkPath.class)) {
                        HyperlinkPath id = (HyperlinkPath) o.getClass().getAnnotation(HyperlinkPath.class);
                        System.out.println(response.matcher);
                        List<Link> links = new LinkedList<Link>();
                        Map<String, String> params = HyperlinkUtils.getHyperlinkValues(o);
                        if(response.matcher != null) {
                            for(Method m : response.matcher.getSuperclass().getMethods()) {
                                if(m.isAnnotationPresent(Hyperlinked.class)) {
                                    String relpath = "/";
                                    Hyperlinked hlr = (Hyperlinked) m.getAnnotation(Hyperlinked.class);
                                    if(m.isAnnotationPresent(Path.class)) {
                                        Path p = (Path)m.getAnnotation(Path.class);
                                        relpath = p.value();
                                    }
                                    System.out.println(response.method);
                                    System.out.println(relpath);
                                    if(relpath.startsWith(response.method)) {
                                        relpath = relpath.substring(response.method.length(), relpath.length());
                                        System.out.println(relpath);
                                        if(m.isAnnotationPresent(GET.class)) {
                                            links.add(Link.fromUri(UriBuilder.fromPath(id.value()).path(relpath).buildFromMap(params)).rel("self").build());
                                        }
                                        if(m.isAnnotationPresent(DELETE.class)) {
                                            links.add(Link.fromUri(UriBuilder.fromPath(id.value()).path(relpath).buildFromMap(params)).rel("remove").build());
                                        }
                                        if(m.isAnnotationPresent(POST.class)) {
                                            links.add(Link.fromUri(UriBuilder.fromPath(id.value()).path(relpath).buildFromMap(params)).rel("build").build());
                                        }
                                        if(m.isAnnotationPresent(PUT.class)) {
                                            links.add(Link.fromUri(UriBuilder.fromPath(id.value()).path(relpath).buildFromMap(params)).rel("update").build());
                                        }
                                    }
                                }
                            }
                        }
                        System.out.println(links);
                        HyperlinkUtils.injectHyperlinks(o, links);
                    }
                }
            }
            return response;
        }
        
    }    
    
}
