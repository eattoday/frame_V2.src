package com.metarnet.core.common.workflow;

import com.metarnet.core.common.exception.AdapterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hadoop
 * Date: 15-5-13
 * Time: 下午3:51
 * To change this template use File | Settings | File Templates.
 */
public class WorkFlowDataConvertor {

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");


    public static Participant convert2Participant(WFParticipant wfparticipant)
            throws AdapterException
    {
        if (wfparticipant == null) {
            return null;
        }
        Participant participant = new Participant();
        if (wfparticipant.getTypeCode().equalsIgnoreCase("person"))
        {
            participant.setParticipantType("1");
        } else if (wfparticipant.getTypeCode().equalsIgnoreCase("role"))
        {
            participant.setParticipantType("2");
        } else if (wfparticipant.getTypeCode().equalsIgnoreCase("organization"))
        {
            participant.setParticipantType("3");
        }
        else {
            throw new AdapterException(wfparticipant.getTypeCode() + "为不支持的类型");
        }
        participant.setParticipantID(wfparticipant.getId());
        if (wfparticipant.getName() != null) {
            participant.setParticipantName(wfparticipant.getName());
        }
//        if (wfparticipant.getAttribute("Permission") != null) {
//            participant.setParticipantStatus(wfparticipant.getAttribute("Permission").toString());
//        }
        return participant;
    }

    public static List<Participant> convert2PartticipantList(List<WFParticipant> wfparticipants)
            throws AdapterException
    {
        List parts = new ArrayList();
        if ((wfparticipants == null) || (wfparticipants.size() == 0)) {
            return parts;
        }
        for (int i = 0; i < wfparticipants.size(); i++) {
            parts.add(convert2Participant((WFParticipant)wfparticipants.get(i)));
        }
        return parts;
    }

    public static List<WFParticipant> convert2WFParticipantList(List<Participant> participants)
            throws AdapterException
    {
        List wfparts = new ArrayList();
        if ((participants == null) || (participants.size() == 0)) {
            return wfparts;
        }
        for (int i = 0; i < participants.size(); i++) {
            wfparts.add(convert2WFParticipant((Participant)participants.get(i)));
        }
        return wfparts;
    }

    public static WFParticipant convert2WFParticipant(Participant participant)
            throws AdapterException
    {
        if (participant == null) {
            return null;
        }
        WFParticipant wfparticipant = new WFParticipant();
        if ("1".equals(String.valueOf(participant.getParticipantType())))
        {
            wfparticipant.setId(participant.getParticipantID());
            wfparticipant.setTypeCode("person");
            wfparticipant.setName(participant.getParticipantName());
        } else if ("2".equals(String.valueOf(participant.getParticipantType())))
        {
            wfparticipant.setId(participant.getParticipantID());
            wfparticipant.setTypeCode("role");
            wfparticipant.setName(participant.getParticipantName());
        } else if ("3".equals(String.valueOf(participant.getParticipantType())))
        {
            wfparticipant.setId(participant.getParticipantID());
            wfparticipant.setTypeCode("organization");
            wfparticipant.setName(participant.getParticipantName());
        } else {
            throw new AdapterException(participant.getParticipantType() + "为不支持的类型");
        }
        return wfparticipant;
    }

}
