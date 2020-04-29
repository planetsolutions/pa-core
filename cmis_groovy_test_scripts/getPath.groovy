import org.apache.chemistry.opencmis.commons.*
import org.apache.chemistry.opencmis.commons.data.*
import org.apache.chemistry.opencmis.commons.enums.*
import org.apache.chemistry.opencmis.client.api.*
import org.apache.chemistry.opencmis.client.util.*

// Количество согласованных перепланировок в многоквартирных домах (МКД)
Document doc = (Document) session.getObject("25c40d3b-2fd8-4bb4-9b9d-fc3deb9337ea");


List<String> paths = doc.getPaths();

System.out.println(paths);


