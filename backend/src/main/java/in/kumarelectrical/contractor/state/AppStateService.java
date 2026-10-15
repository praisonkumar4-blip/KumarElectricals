package in.kumarelectrical.contractor.state;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
public class AppStateService {
    private static final String KEY="main";
    private final AppStateRepository repository;
    private final ObjectMapper mapper;
    public AppStateService(AppStateRepository repository, ObjectMapper mapper) { this.repository=repository; this.mapper=mapper; }

    @Transactional(readOnly=true)
    public ObjectNode read() {
        return repository.findById(KEY).map(row -> parse(row.getPayload())).orElseGet(this::emptyState);
    }
    @Transactional
    public ObjectNode write(JsonNode input) {
        ObjectNode clean=mapper.createObjectNode();
        clean.set("employees", array(input,"employees")); clean.set("sites",array(input,"sites"));
        clean.set("attendance",array(input,"attendance")); clean.set("projects",array(input,"projects"));
        clean.set("advances", input!=null && input.path("advances").isObject()?input.path("advances").deepCopy():mapper.createObjectNode());
        clean.set("deductions", input!=null && input.path("deductions").isObject()?input.path("deductions").deepCopy():mapper.createObjectNode());
        repository.save(new AppStateEntity(KEY,clean.toString())); return clean;
    }
    public ObjectNode forEmployee(String employeeId) {
        ObjectNode full=read(), view=mapper.createObjectNode();
        ArrayNode employees=mapper.createArrayNode(), sites=mapper.createArrayNode(), attendance=mapper.createArrayNode(), projects=mapper.createArrayNode();
        JsonNode employee=null;
        for(JsonNode e:full.path("employees")) if(employeeId.equals(e.path("id").asText())) employee=e;
        if(employee!=null) employees.add(employee);
        java.util.Set<String> siteIds=new java.util.HashSet<>();
        if(employee!=null && !employee.path("siteId").asText("").isBlank()) siteIds.add(employee.path("siteId").asText());
        for(JsonNode a:full.path("attendance")) if(employeeId.equals(a.path("employeeId").asText())) { attendance.add(a); if(!a.path("siteId").asText("").isBlank())siteIds.add(a.path("siteId").asText()); }
        for(JsonNode s:full.path("sites")) if(siteIds.contains(s.path("id").asText())) sites.add(s);
        for(JsonNode p:full.path("projects")) if(siteIds.contains(p.path("siteId").asText())) projects.add(p);
        view.set("employees",employees);view.set("sites",sites);view.set("attendance",attendance);view.set("projects",projects);
        view.set("advances",mapper.createObjectNode());view.set("deductions",mapper.createObjectNode()); return view;
    }
    public String employeeName(String employeeId) { for(JsonNode e:read().path("employees")) if(employeeId.equals(e.path("id").asText())) return e.path("name").asText(employeeId); return employeeId; }
    private ArrayNode array(JsonNode node,String field) { JsonNode v=node==null?null:node.path(field); return v!=null&&v.isArray()?(ArrayNode)v.deepCopy():mapper.createArrayNode(); }
    private ObjectNode parse(String json) { try { JsonNode n=mapper.readTree(json); return n!=null&&n.isObject()?(ObjectNode)n:emptyState(); } catch(Exception e) { throw new IllegalStateException("Stored application data is invalid JSON",e); } }
    private ObjectNode emptyState() {
        ObjectNode root=mapper.createObjectNode(); ArrayNode employees=mapper.createArrayNode(),sites=mapper.createArrayNode(),attendance=mapper.createArrayNode(),projects=mapper.createArrayNode();
        ObjectNode s1=mapper.createObjectNode();s1.put("id","SITE001");s1.put("name","Sevalaperi Headroom");s1.put("customer","Water Supply Board");s1.put("address","Sevalaperi, Tirunelveli");s1.put("supervisor","R. Kumar");s1.put("start",LocalDate.now().toString());s1.put("end",LocalDate.now().plusDays(60).toString());s1.put("status","Active");s1.put("budget",9000);s1.put("description","Electrical drive installation and maintenance");s1.put("lat","");s1.put("lng","");s1.put("radius",150);s1.put("photo","");sites.add(s1);
        ObjectNode s2=mapper.createObjectNode();s2.put("id","SITE002");s2.put("name","Kaliyavoor Pump Room");s2.put("customer","Pump Room Maintenance");s2.put("address","Kaliyavoor, Tamil Nadu");s2.put("supervisor","S. Mani");s2.put("start",LocalDate.now().minusDays(30).toString());s2.put("end",LocalDate.now().plusDays(30).toString());s2.put("status","Active");s2.put("budget",7000);s2.put("description","11kV VCB panel inspection and repair");s2.put("lat","");s2.put("lng","");s2.put("radius",150);s2.put("photo","");sites.add(s2);
        employees.add(employee("EMP001","Arun Kumar","9876543210","Electrician",800,"SITE001"));employees.add(employee("EMP002","M. Selvam","9876543211","Technician",950,"SITE002"));employees.add(employee("EMP003","Suresh","9876543212","Helper",600,"SITE001"));
        ObjectNode p1=mapper.createObjectNode();p1.put("id","PRJ1001");p1.put("title","Electrical Installation");p1.put("siteId","SITE001");p1.put("category","Electrical");p1.put("date",LocalDate.now().toString());p1.put("description","Drive installation and site wiring support.");p1.put("photo","");projects.add(p1);
        root.set("employees",employees);root.set("sites",sites);root.set("attendance",attendance);root.set("projects",projects);root.set("advances",mapper.createObjectNode());root.set("deductions",mapper.createObjectNode());return root;
    }
    private ObjectNode employee(String id,String name,String mobile,String role,int wage,String siteId){ObjectNode e=mapper.createObjectNode();e.put("id",id);e.put("name",name);e.put("mobile",mobile);e.put("role",role);e.put("wage",wage);e.put("joining",LocalDate.now().minusMonths(6).toString());e.put("siteId",siteId);e.put("status","Active");e.put("address","Tirunelveli");return e;}
}
