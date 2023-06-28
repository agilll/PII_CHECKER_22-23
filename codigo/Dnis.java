/******************************************************************************************************
 *    PROGRAMACIÓN II
 *    EE TELECOMUNICACIÓN
 *    UNIVERSIDAD DE VIGO
 *
 *    PII Checker
 *
 *    Autor: Alberto Gil Solla
 *    Curso : 2022-2023
 *    
 *    Programa para corregir automáticamente los resultados de las prácticas de PII del curso 2022-2023
 *    
 *    Requiere que en el directorio $SINT0_PII estén las baterías de pruebas
 *    
 ******************************************************************************************************/


package docencia.pii.checker23;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

import jakarta.servlet.ServletConfig;

public class Dnis {

		static HashMap<Integer, String> mapDnis = new HashMap<Integer, String>();   // el hashmap de cuenta/dni

 		public static void readDnis(ServletConfig config, String fileName) throws FileNotFoundException {
 			
 			String pathWebapps = config.getServletContext().getRealPath(".");
 			String pathDnis = pathWebapps+"/WEB-INF/classes/docencia/pii/checker23/"+fileName;
 			
 			Scanner input = new Scanner(new File(pathDnis));

 			String[] miarray;
 			String linea;

 			while (input.hasNext()) {
 				linea = input.nextLine().trim();

 				miarray = linea.split(",");
 				
 				mapDnis.put(Integer.parseInt(miarray[0]), miarray[1]);
 			}
 			
 			System.out.println("**** Número de DNIs leídos: "+mapDnis.size());
 		}
 		
 		public static boolean checkDni(int cuenta, String dni) {
 			
 			String elVerdaderoDni = mapDnis.get(cuenta);
 			
 			if (elVerdaderoDni == null) {
 				System.out.println("**** Comprobando DNI. Cuenta: "+cuenta+" --  Supuesto DNI: #"+ dni+ "# --  DNI real: #null#");
 				return false;
 			}
 			
 			if (elVerdaderoDni.toLowerCase().equals(dni.toLowerCase())) return true;
 			else {
 				System.out.println("**** Comprobando DNI. Cuenta: "+cuenta+" --  Supuesto DNI: #"+ dni+ "# --  DNI real: #"+elVerdaderoDni+"#");
 				return false;
 			}
 		}

}