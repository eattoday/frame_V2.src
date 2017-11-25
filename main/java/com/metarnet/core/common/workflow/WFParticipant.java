package com.metarnet.core.common.workflow;

import commonj.sdo.DataObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/7/20/0020.
 */
public class WFParticipant {

    private static final long serialVersionUID = 3907501211444572364L;
    String id;
    String name;
    String typeCode;
    Map<Object, Object> attributes = new HashMap();

    public WFParticipant(String id, String name, String typeCode) {
        this.id = id;
        this.name = name;
        this.typeCode = typeCode;
    }

    public WFParticipant(String id, String name, String email, String typeCode) {
        this.id = id;
        this.name = name;
        this.typeCode = typeCode;
        this.setAttribute("email", email);
    }

    public WFParticipant() {
        this.id = "";
        this.name = "";
        this.typeCode = "";
    }

    public WFParticipant(String id, String typeCode) {
        this.id = id;
        this.name = "";
        this.typeCode = typeCode;
    }

    public WFParticipant(DataObject o) {
        this.id = o.getString("id");
        this.name = o.getString("name");
        this.typeCode = o.getString("typeCode");
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /** @deprecated */
    @Deprecated
    public String getEmail() {
        return (String)this.getAttribute("email");
    }

    /** @deprecated */
    @Deprecated
    public void setEmail(String email) {
        this.setAttribute("email", email);
    }

    public String getTypeCode() {
        return this.typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public boolean equals(Object o) {
        if(o == null) {
            return false;
        } else if(o.getClass() != this.getClass()) {
            return false;
        } else {
            WFParticipant wfp = (WFParticipant)o;
            if(this.id == null) {
                if(wfp.getId() != null) {
                    return false;
                }
            } else if(!this.id.equals(wfp.getId())) {
                return false;
            }

            if(this.typeCode == null) {
                if(wfp.getTypeCode() != null) {
                    return false;
                }
            } else if(!this.typeCode.equals(wfp.getTypeCode())) {
                return false;
            }

            return true;
        }
    }

    public String toString() {
        return this.id + "_" + this.typeCode;
    }

    public Object getAttribute(Object key) {
        return this.attributes.get(key);
    }

    public void setAttribute(Object key, Object value) {
        this.attributes.put(key, value);
    }
}
