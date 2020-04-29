import org.apache.chemistry.opencmis.commons.*
import org.apache.chemistry.opencmis.commons.data.*
import org.apache.chemistry.opencmis.commons.enums.*
import org.apache.chemistry.opencmis.client.api.*
import org.apache.chemistry.opencmis.client.util.*



System.out.println(session.getObjectByPath("/Ростелеком/Входящие документы/Архив/Проверка сотрудников/Ivanov"));
System.out.println(session.getObjectByPath("/Ростелеком/Входящие документы/Проверка сотрудников/Ivanov"));


Folder folder1 = (Folder) session.getObject("242d5169-36a3-4a8d-900d-096766d7db19");

//Folder folder2 = (Folder) session.getObject("6c8d25eb-1cc3-47c6-ba84-2daf13e62608");

System.out.println(folder1.getName());

//System.out.println(folder2.getName());


