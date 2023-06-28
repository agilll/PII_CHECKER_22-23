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

import java.util.HashMap;

public class Msgs {

		public static final String CREATED = "2023";

		static HashMap<Integer, String[]> mapMsgs = new HashMap<Integer, String[]>();   // el hashmap de mensajes

		public static final int M00=0,  M01=1,  M02=2,  M03=3,  M04=4,  M05=5,  M06=6,  M07=7,  M08=8,  M09=9;
		public static final int M10=10, M11=11, M12=12, M13=13, M14=14, M15=15, M16=16, M17=17, M18=18, M19=19;
		public static final int M20=20, M21=21, M22=22, M23=23, M24=24, M25=25, M26=26, M27=27, M28=28, M29=29;
		public static final int M30=30, M31=31, M32=32, M33=33, M34=34, M35=35, M36=36, M37=37, M38=38, M39=39;
		public static final int M40=40, M41=41, M42=42, M43=43, M44=44, M45=45, M46=46, M47=47, M48=48;
		public static final int M50=50, M51=51, M52=52, M53=53,         M55=55, M56=56, M57=57;

		static {
			mapMsgs.put(M00, new String[] {"Servicio de corrección", "Checking service"});
			mapMsgs.put(M01, new String[] {"Programación II", "Programming II"});
			mapMsgs.put(M02, new String[] {"EE Telecomunicación (Universidad de Vigo)", "EE Telecomunicación (University of Vigo)"});
			mapMsgs.put(M03, new String[] {"SERVICIO DE VERIFICACIÓN DE PRÁCTICAS, CURSO 22-23", "Practice Checking Service, Course 22-23"});
			mapMsgs.put(M04, new String[] {"Inicio", "Home"});
			mapMsgs.put(M05, new String[] {"Introduce el número de la cuenta ", "Type the account number "});
			mapMsgs.put(M06, new String[] {"Selecciona la práctica a la que quieras pasarle las baterías", "Select the practice to be corrected"});
			mapMsgs.put(M07, new String[] {"ENTREGABLE", "DELIVERABLE"});
			mapMsgs.put(M08, new String[] {"tarea", "task"});
			mapMsgs.put(M09, new String[] {"actividad", "activity"});
			mapMsgs.put(M10, new String[] {"Comprobar", "Check"});
			
			mapMsgs.put(M11, new String[] {"No se recibió número de cuenta", "Account number was not received"});
			mapMsgs.put(M12, new String[] {"El directorio raíz de la cuenta %s no tiene permisos 750", "Account %s root dir does not have 750 permissions"});
			mapMsgs.put(M13, new String[] {"Practicas", "Lab"});
			mapMsgs.put(M14, new String[] {"El directorio '%s' no existe en la raíz de la cuenta pii%s, o no tiene permisos (755) para entrar", "'%s' folder does not exist in the account pii%s root folder, or it has not permissions (755) to enter"});
			mapMsgs.put(M15, new String[] {"No se indicó ninguna práctica para corregir", "Practice to be corrected was not specified"});
			mapMsgs.put(M16, new String[] {"Solicitada la correción de una práctica desconocida", "Unknown practice requested"});
			mapMsgs.put(M17, new String[] {"Corrigiendo la tarea 2b de la P2 de pii", "Checking task 2b of practice P2 of pii"});
			mapMsgs.put(M18, new String[] {"En el directorio '%s' no está el fichero ", "In folder '%s', it is not the file "});
			mapMsgs.put(M19, new String[] {"No existe (o no se puede entrar en) el directorio ","Cannot get into folder "});
			mapMsgs.put(M20, new String[] {"Fichero inicial de clientes", "Clients input file"});
			
			mapMsgs.put(M21, new String[] {"Fichero final de clientes esperado", "Expected clients output file"});
			mapMsgs.put(M22, new String[] {"Errores en la ejecución de la práctica", "Errors running the practice"});
			mapMsgs.put(M23, new String[] {"Errores en la ejecución del diff", "Errors running diff"});
			mapMsgs.put(M24, new String[] {"Diferencias respecto al resultado correcto", "Differences with expected result"});
			mapMsgs.put(M25, new String[] {"NINGUNA DIFERENCIA: TODO BIEN", "OK, no difference"});
			mapMsgs.put(M26, new String[] {"Fichero de salida que produce la práctica", "Output file generated by the practice"});
			mapMsgs.put(M27, new String[] {"Interfaz del profesor", "Teacher's interface"});
			mapMsgs.put(M28, new String[] {"Introduce tu NIF ", "Type your ID "});
			mapMsgs.put(M29, new String[] {"El NIF no se corresponde con la cuenta", "ID incorrect for such account"});
			
			
			mapMsgs.put(M30, new String[] {"Corrigiendo la actividad 4 de la P4 de pii", "Checking activity 4 of practice P4 of pii"});
			mapMsgs.put(M31, new String[] {"Fichero inicial del hotel", "Hotel input file"});
			mapMsgs.put(M32, new String[] {"Fichero de entradas y salidas del hotel", "Hotel I/O file"});
			mapMsgs.put(M33, new String[] {"Fichero TXT final del hotel esperado", "Expected hotel output TXT file"});
			mapMsgs.put(M34, new String[] {"Corregir el %s de todos los alumnos", "Check %s for all students"});
			mapMsgs.put(M35, new String[] {"(0-200)<br>Prácticas %s válidas", "(201-220)<br>Valid %s practices"});
			mapMsgs.put(M36, new String[] {"El programa no terminó tras 1 segundo", "Program did not finished after 1 second"});
			mapMsgs.put(M37, new String[] {"El resto de los tests no proceden en este escenario", "The other tests are not applicable in this scenery"});
			mapMsgs.put(M38, new String[] {"Fichero TXT final de puntos esperado", "Excpected points clients output TXT file"});
			mapMsgs.put(M39, new String[] {"Fichero TXT final de pagos esperado", "Excpected payments output TXT file"});
			
			mapMsgs.put(M40, new String[] {"Corrigiendo la %s de pii", "Checking practice %s of pii"});
			mapMsgs.put(M41, new String[] {"Fichero TXT final de clientes esperado", "Excpected clients output TXT file"});
			mapMsgs.put(M42, new String[] {"Fichero con el mapa final del hotel esperado", "Excpected hotel map output file"});
			mapMsgs.put(M43, new String[] {"Comprobando diferencias en el fichero final del hotel", "Checking differences in final hotel file"});
			mapMsgs.put(M44, new String[] {"Comprobando diferencias en el fichero final de clientes", "Checking differences in final clients file"});
			mapMsgs.put(M45, new String[] {"Comprobando diferencias en el fichero del mapa del hotel", "Checking differences in hotel map file"});
			mapMsgs.put(M46, new String[] {"No hay fichero de mapa", "No map file"});
			mapMsgs.put(M47, new String[] {"Fichero con los mensajes que imprime la práctica", "File containing practice output messages"});
			mapMsgs.put(M48, new String[] {"La práctica no ha generado ningún mensaje", "Practice has not printed output messages"});
			
			mapMsgs.put(M50, new String[] {"Comprobando diferencias en el fichero final binario de clientes", "Checking differences in final binary clients file"});
			mapMsgs.put(M51, new String[] {"Comprobando diferencias en el fichero final binario de puntos", "Checking differences in final binary points file"});
			mapMsgs.put(M52, new String[] {"Comprobando diferencias en el fichero final de texto de puntos", "Checking differences in final txt points file"});
			mapMsgs.put(M53, new String[] {"Comprobando diferencias en el fichero final de una imagen", "Checking differences in a final image file"});
			mapMsgs.put(M55, new String[] {"Comprobando diferencias en el fichero final de texto de pagos", "Checking differences in final txt payments file"});
			mapMsgs.put(M56, new String[] {"Fichero binario final de clientes esperado", "Excpected clients output binary file"});
			mapMsgs.put(M57, new String[] {"Fichero binario final de puntos esperado", "Excpected  points output binary file"});
			
			
		}


		 // obtiene un mensaje (id) dependiendo del idioma (lang = "es" o "en")
 		static String getMsg(int id, String lang) {
 			 String value[] = mapMsgs.get(id);
			 if (value == null) return "ERROR MsCh.getMsg key "+Integer.toString(id);
 			 if (lang.equals("en"))  return value[1];
 			 else return value[0];
 		}


}