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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PIIChecker extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = null;  // el objeto Logger
	
	private static final String ES_URI = "PIICheck";    // URI for Spanish students
	private static final String EN_URI = "PIIeCheck";   // URI for English students
	private static final String P_ES_URI = "CheckPII";  // URI for Spanish prof
	private static final String P_EN_URI = "CheckPIIe"; // URI for English prof
	
	private static final String  PII = "/home/eetlabs.local/pii/";  // directorio base de las cuentas de PII
	// private static final String  SINT0_PII = "/home/eetlabs.local/sint/sint0/public_html/webapps/pii/"; // directorio base donde se guardarán los resultados de las ejecuciones
	private static String  SINT0_PII_RT = "";   // directorio base de las cuentas de PII, calculado al ejecutarse
	
	private static final String  JAVA = "/usr/lib/jvm/java-11-openjdk-amd64/bin/java";  // ubicación de la JVM usada
	
	// posibles resultados de cada test
	private static final int TEST_OK = 0;
	private static final int ERROR_JAVA = 1;
	private static final int ERROR_DIFF = 2;
	private static final int DIFFS = 3;
	
	

	// para inicializar el objeto Logger, que imprimirá en el fichero "piiprof.log", que se crea donde arranca el tomcat
	// ese nombre se especifica en el fichero de configuración log4j2.xml
	// log4j2.xml se busca en el classpath, yo lo tengo en "classes"
	
	public static void initLoggerPIIChecker (Class<PIIChecker> c) {
		logger = LogManager.getLogger("piiLogger"); // "piiLogger" es el nombre dado en log4j2.xml
		
		if (logger == null) System.out.println("************ loger es: "+logger);
	}
	
	 public static void logPIIChecker (String msg) {
		System.out.println("************ imprimiendo mensaje en el loger: "+msg);
		logger.info(msg); 
	 }
	
	 
	public void init (ServletConfig servletConfig) throws ServletException  {
		PIIChecker.initLoggerPIIChecker(PIIChecker.class);
		logPIIChecker("# Init...");
		
		PIIChecker.SINT0_PII_RT = servletConfig.getServletContext().getRealPath("/pii/");
		System.out.println("SINT_PII_RT="+SINT0_PII_RT);
		
		try {
			Dnis.readDnis(servletConfig, "dnis.csv");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("************  FileNotFoundException al leer los DNIs");
			e.printStackTrace();
		}
	}

	
	// procesa todas las solicitudes a este servlet

	public void doGet(HttpServletRequest request, HttpServletResponse response)
				throws IOException, ServletException
	{

		// por ahora no se tiene en cuenta si es el profesor 
		
		String uri = request.getRequestURI();
		
		// se averigua el idioma según la URI invocada
		String cLang="es";
		if (uri.endsWith(PIIChecker.EN_URI)  ||  uri.endsWith(PIIChecker.P_EN_URI)  )
			cLang = "en";

		// se averigua si es profesor según la URI invocada
		boolean esProfesor = false;  
		if (uri.endsWith(PIIChecker.P_EN_URI)  ||  uri.endsWith(PIIChecker.P_ES_URI))
			 esProfesor = true;

		
		response.setCharacterEncoding("utf-8");

		// en 'screenP' se recibe la pantalla que se está solicitando

		String screenP = request.getParameter("screenP");

		if (screenP == null) screenP="0"; // si screenP no existe, se está pidiendo la pantalla inicial (igual a screenP=0)

		switch (screenP) {
			case "0":		// screenP=0, se está pidiendo la pantalla inicial del checker
				if (esProfesor) this.doGetHomeProf(request, response, cLang);
				else this.doGetHomeStudent(request, response, cLang);
				break;
	
			case "1":		// screenP=1, se está pidiendo la corrección de alguna práctica
				this.doGetCorrect(request, response, cLang, esProfesor);
				break;
			
			case "21":		// screenP=2, se está pidiendo la corrección de todo
				this.doGetCorrectAllE1(request, response, cLang, esProfesor);
				break;
			
			case "22":		// screenP=2, se está pidiendo la corrección de todo
				this.doGetCorrectAllE2(request, response, cLang, esProfesor);
				break;
			case "23":		// screenP=2, se está pidiendo la corrección de todo
				this.doGetCorrectAllE3(request, response, cLang, esProfesor);
				break;
		}
	}

	
	
	public void doPrintTable(PrintWriter out, String lang, boolean esProfesor) {
		
        // el parámetro "practica" indicará qué práctica se quiere corregir   
		
	    out.println("<table border='1'>");
	    
	    // ENTREGABLE E1 
	    // M07 = ENTREGABLE
	    out.print("<tr><td rowspan='2'> <b>"+Msgs.getMsg(Msgs.M07, lang)+" 1</b>");
	    // M08 = tarea
	    out.println("<td><input type='radio' name='practica' value='p2' checked> P2 "+Msgs.getMsg(Msgs.M08, lang)+" 2b");
	    out.println("<tr>");
	    // M09 = actividad
	    out.println("<td><input type='radio' name='practica' value='p4'> P4 "+Msgs.getMsg(Msgs.M09, lang)+" 4");
	    
	    
	    
	    // ENTREGABLE E2 
	    // M07 = ENTREGABLE
	    out.print("<tr><td> <b>"+Msgs.getMsg(Msgs.M07, lang)+" 2</b>");
	    out.println("<td><input type='radio' name='practica' value='p9'> P9 ");
	    
	    
	    // ENTREGABLE E3 
	 // M07 = ENTREGABLE
	    out.print("<tr><td rowspan='3'> <b>"+Msgs.getMsg(Msgs.M07, lang)+" 3</b>");
	    out.println("<td>");
	    
		out.println("<input type='radio' name='practica' value='p10'> P10 ");
		out.println("<tr>");
		
	    out.println("<td><input type='radio' name='practica' value='p11'> P11 ");
	    out.println("<tr>");
		    
		out.println("<td><input type='radio' name='practica' value='p12'> P12 ");
	    
	    
	    out.println("</table>");
	}	
	
	
	
	
	
	// piden la pantalla inicial para pedir una corrección por un profesor

	public void doGetHomeProf (HttpServletRequest request, HttpServletResponse response, String lang)
			throws IOException
	{
		String displayTablePracticas = "none";
		
		String cuenta = request.getParameter("cuenta");
		if (cuenta == null) cuenta = "";
		
		if (cuenta.length() > 0) displayTablePracticas = "block";
		
		PrintWriter out = response.getWriter();
		
		this.printHeader(out, lang);
		
		// M27 = Interfaz del profesor
		out.print("<div id='interfaz'>"+Msgs.getMsg(Msgs.M27, lang)+"</div>");

        out.println("<script>");
        out.println("function tooglePractices() { ");
        out.println("   divTableElement = document.getElementById('tablaPracticas');");
        out.println("   valAccount = document.getElementById('numCuenta').value;");
        out.println("   let isNum = /^\\d+$/.test(valAccount);");
        out.println("   numAccount = parseInt(valAccount);");
        out.println("   if ((!isNum) || (numAccount < 0) || (numAccount > 250) ) divTableElement.style.display = 'none';");
        out.println("   else divTableElement.style.display = 'block';");
        out.println("}");
        out.println("</script>");

        // M34 = Corregir el E1 de todos los alumnos
        out.print("<h3><a href='?screenP=21'>"+String.format(Msgs.getMsg(Msgs.M34, lang), "E1")+"</a></h3>");
        
        // M34 = Corregir el E2 de todos los alumnos
        out.print("<h3><a href='?screenP=22'>"+String.format(Msgs.getMsg(Msgs.M34, lang), "E2")+"</a></h3>");
        
        // M34 = Corregir el E3 de todos los alumnos
        out.print("<h3><a href='?screenP=23'>"+String.format(Msgs.getMsg(Msgs.M34, lang), "E3")+"</a></h3>");
        
        out.println("<form action='?' method='GET'>");
        
        // M05 = Introduce el número de la cuenta                                                                                                
        out.print("<h3>"+Msgs.getMsg(Msgs.M05, lang));
        out.println("<input id='numCuenta' type='text' name='cuenta' value='"+cuenta+"' size='4' style='border-width: 1px' onkeypress='return event.keyCode != 13;' onkeyup='tooglePractices()' required></h3>");                                                                           
        
        out.println("<div id='tablaPracticas' style='display: "+displayTablePracticas+"'>");

		// M06 = Selecciona la práctica a la que quieras pasarle las baterías
        out.println("<h3>"+Msgs.getMsg(Msgs.M06, lang)+"</h3>");
        
        doPrintTable(out, lang, true);
       
        out.println("<input type='hidden' name='screenP' value='1'>");
        
        // M10 = Comprobar
        out.println("<p><input class='enviar' type='submit' value='"+Msgs.getMsg(Msgs.M10, lang)+"'>");
        out.println("</div>");
        
        
        out.println("</form>");
        
		printCopyright(out); 
	}
	
	
	
	

	// piden la pantalla inicial para pedir una corrección por un profesor

		public void doGetHomeStudent (HttpServletRequest request, HttpServletResponse response, String lang)
				throws IOException
		{
			String displayTablePracticas = "none";
			
			String cuenta = request.getParameter("cuenta");
			if (cuenta == null) cuenta = "";
			
			String dni = request.getParameter("dni");
			if (dni == null) dni = "";
			
			if (!cuenta.equals("") && !dni.equals(""))  displayTablePracticas = "block";
			
			PrintWriter out = response.getWriter();
			
			this.printHeader(out, lang);
			
			// out.println("<h1>Cambiando cosas, no usar</h1>");


	        out.println("<script>");
	        out.println("function tooglePractices() { ");
	        out.println("   divTableElement = document.getElementById('tablaPracticas');");
	        out.println("   valAccount = document.getElementById('numCuenta').value;");
	        out.println("   valDni = document.getElementById('numDni').value;");
	        out.println("   let isNum = /^\\d+$/.test(valAccount);");
	        out.println("   numAccount = parseInt(valAccount);");
	        out.println("   if ((!isNum) || (numAccount < 0) || (numAccount > 250) || (valDni.length == 0) ) divTableElement.style.display = 'none';");
	        out.println("   else divTableElement.style.display = 'block';");
	        out.println("}");
	        out.println("</script>");

	        out.println("<form action='?' method='GET'>");

	        
	        // M05 = Introduce el número de la cuenta                                                                                                
	        out.print("<h3>"+Msgs.getMsg(Msgs.M05, lang));
	        out.println("<input id='numCuenta' type='text' name='cuenta' value='"+cuenta+"' size='4' style='border-width: 1px' onkeypress='return event.keyCode != 13;' onkeyup='tooglePractices()' required></h3>");
	        
	        // M05 = Introduce tu NIF                                                                                                
	        out.print("<h3>"+Msgs.getMsg(Msgs.M28, lang));
	        out.println("<input id='numDni' type='text' name='dni' value='"+dni+"' size='10' style='border-width: 1px' onkeypress='return event.keyCode != 13;' onkeyup='tooglePractices()' required></h3>");
	        
	        // el parámetro "practica" indicará qué práctica se quiere corregir                                                                              

	        out.println("<div id='tablaPracticas' style='display: "+displayTablePracticas+"'>");

	        
			// M06 = Selecciona la práctica a la que quieras pasarle las baterías
	        out.println("<h3>"+Msgs.getMsg(Msgs.M06, lang)+"</h3>");
	        
	        doPrintTable(out, lang, false);
	       
	        out.println("<input type='hidden' name='screenP' value='1'>");
	        
	        // M10 = Comprobar

	        out.println("<p><input class='enviar' type='submit' value='"+Msgs.getMsg(Msgs.M10, lang)+"'>");
	        
	        out.println("</div>");
	        
	        
	        out.println("</form>");

			printCopyright(out); 
		}
		
		
		
		
		
	public void doGetCorrectAllE1 (HttpServletRequest request, HttpServletResponse response, String lang, boolean esProfesor) 
			throws IOException{
		
		String rootFolder, practicasFolder;
		File fd;
		int resultado, initAccount, finalAccount;
		ArrayList<Integer> practicas2Validas = new ArrayList<Integer>();
		ArrayList<Integer> practicas4Validas = new ArrayList<Integer>();
		
		System.out.println("****** Vamos a corregir en background todas...");
		
		PrintWriter out = new PrintWriter("/dev/null");
		
		if (lang.equals("es")) {
			initAccount=0;
			finalAccount=200;
		}
		else {
			initAccount=201;
			finalAccount=220;
		}
		
		for (int i=initAccount; i<=finalAccount; i++) {

			System.out.println("****** Vamos a corregir en background la "+i);
			
			rootFolder = PII+"pii"+i+"/";
			fd = new File (rootFolder);
			if (!fd.canRead()) continue;   // no se puede entrar en la cuenta a corregir
			
			// M13 = Practicas
			practicasFolder = rootFolder+Msgs.getMsg(Msgs.M13, lang);  // las prácticas están en el directorio 'Practicas'
			fd = new File (practicasFolder);
		    if (!fd.canRead()) continue;  // no existe o no se puede entrar en el direcorio donde están las prácticas
				
			try {	
				resultado = this.doGetCorrectP2(out, ""+i, lang, esProfesor);
				//System.out.println("****** La "+i+" dio como resultado "+resultado);
				if (resultado == 0) practicas2Validas.add(Integer.valueOf(i));
			}
			catch(Exception e) {}
			
			try {	
				resultado = this.doGetCorrectP4(out, ""+i, lang, esProfesor);
				//System.out.println("****** La "+i+" dio como resultado "+resultado);
				if (resultado == 0) practicas4Validas.add(Integer.valueOf(i));				
			}
			catch(Exception e) {}
		}
		
		out = response.getWriter();
		this.printHeader(out, lang);
		
		out.println("Estas son las prácticas que han pasado todos los tests");
		
		// imprimir las prácticas válidas
		// M35 = Prácticas %s válidas
		out.print("<h3 class='bien'>"+String.format(Msgs.getMsg(Msgs.M35, lang), "P2")+"("+practicas2Validas.size()+"): ");
		for (int i=0; i<practicas2Validas.size(); i++)
			if (i == practicas2Validas.size()-1) out.print(practicas2Validas.get(i));
			else out.print(practicas2Validas.get(i)+", ");
		out.print("</h3>");
		
		// M35 = Prácticas %s válidas
		out.print("<h3 class='bien'>"+String.format(Msgs.getMsg(Msgs.M35, lang), "P4")+"("+practicas4Validas.size()+"): ");
		for (int i=0; i<practicas4Validas.size(); i++)
			if (i == practicas4Validas.size()-1) out.print(practicas4Validas.get(i));
			else out.print(practicas4Validas.get(i)+", ");
		out.print("</h3>");
		
		this.printBBC(out, lang, "", "");  // pie de la página	
	}
		
	
	
	
	
	
	
public void doGetCorrectAllE2 (HttpServletRequest request, HttpServletResponse response, String lang, boolean esProfesor) 
		throws IOException{
	
	String rootFolder, practicasFolder;
	File fd;
	int resultado, initAccount, finalAccount;
	ArrayList<Integer> practicasP9Validas = new ArrayList<Integer>();
	
	System.out.println("****** Vamos a corregir en background todas...");
	
	PrintWriter out = new PrintWriter("/dev/null");
	
	if (lang.equals("es")) {
		initAccount=0;
		finalAccount=200;
	}
	else {
		initAccount=201;
		finalAccount=220;
	}
	
	for (int i=initAccount; i<=finalAccount; i++) {

		System.out.println("****** Vamos a corregir en background la "+i);
		
		rootFolder = PII+"pii"+i+"/";
		fd = new File (rootFolder);
		if (!fd.canRead()) continue;   // no se puede entrar en la cuenta a corregir
		
		// M13 = Practicas
		practicasFolder = rootFolder+Msgs.getMsg(Msgs.M13, lang);  // las prácticas están en el directorio 'Practicas'
		fd = new File (practicasFolder);
	    if (!fd.canRead()) continue;  // no existe o no se puede entrar en el direcorio donde están las prácticas
			
		try {	
			resultado = this.doGetCorrectP9(out, ""+i, lang, esProfesor);
			//System.out.println("****** La "+i+" dio como resultado "+resultado);
			if (resultado == 0) practicasP9Validas.add(Integer.valueOf(i));
		}
		catch(Exception e) {}
	}
	
	out = response.getWriter();
	this.printHeader(out, lang);
	
	out.println("Estas son las prácticas que han pasado todos los tests");
	
	// imprimir las prácticas válidas
	// M35 = Prácticas %s válidas
	out.print("<h3 class='bien'>"+String.format(Msgs.getMsg(Msgs.M35, lang), "P9")+"("+practicasP9Validas.size()+"): ");
	for (int i=0; i<practicasP9Validas.size(); i++)
		if (i == practicasP9Validas.size()-1) out.print(practicasP9Validas.get(i));
		else out.print(practicasP9Validas.get(i)+", ");
	out.print("</h3>");
	
	
	this.printBBC(out, lang, "", "");  // pie de la página	
}
	




public void doGetCorrectAllE3 (HttpServletRequest request, HttpServletResponse response, String lang, boolean esProfesor) 
		throws IOException{
	
	String rootFolder, practicasFolder;
	File fd;
	int resultado, initAccount, finalAccount;
	ArrayList<Integer> practicas10Validas = new ArrayList<Integer>();
	ArrayList<Integer> practicas11Validas = new ArrayList<Integer>();
	ArrayList<Integer> practicas12Validas = new ArrayList<Integer>();
	
	System.out.println("****** Vamos a corregir en background todas...");
	
	PrintWriter out = new PrintWriter("/dev/null");
	
	if (lang.equals("es")) {
		initAccount=0;
		finalAccount=200;
	}
	else {
		initAccount=201;
		finalAccount=220;
	}
	
	for (int i=initAccount; i<=finalAccount; i++) {

		System.out.println("****** Vamos a corregir en background la "+i);
		
		rootFolder = PII+"pii"+i+"/";
		fd = new File (rootFolder);
		if (!fd.canRead()) continue;   // no se puede entrar en la cuenta a corregir
		
		// M13 = Practicas
		practicasFolder = rootFolder+Msgs.getMsg(Msgs.M13, lang);  // las prácticas están en el directorio 'Practicas'
		fd = new File (practicasFolder);
	    if (!fd.canRead()) continue;  // no existe o no se puede entrar en el direcorio donde están las prácticas
			
		try {	
			resultado = this.doGetCorrectP10(out, ""+i, lang, esProfesor);
			//System.out.println("****** La "+i+" dio como resultado "+resultado);
			if (resultado == 0) practicas10Validas.add(Integer.valueOf(i));
		}
		catch(Exception e) {}
		
		try {	
			resultado = this.doGetCorrectP11(out, ""+i, lang, esProfesor);
			//System.out.println("****** La "+i+" dio como resultado "+resultado);
			if (resultado == 0) practicas11Validas.add(Integer.valueOf(i));				
		}
		catch(Exception e) {}
		
		try {	
			resultado = this.doGetCorrectP12(out, ""+i, lang, esProfesor);
			//System.out.println("****** La "+i+" dio como resultado "+resultado);
			if (resultado == 0) practicas12Validas.add(Integer.valueOf(i));				
		}
		catch(Exception e) {}
	}
	
	out = response.getWriter();
	this.printHeader(out, lang);
	
	out.println("Estas son las prácticas que han pasado todos los tests");
	
	// imprimir las prácticas válidas
	// M35 = Prácticas %s válidas
	out.print("<h3 class='bien'>"+String.format(Msgs.getMsg(Msgs.M35, lang), "P10")+"("+practicas10Validas.size()+"): ");
	for (int i=0; i<practicas10Validas.size(); i++)
		if (i == practicas10Validas.size()-1) out.print(practicas10Validas.get(i));
		else out.print(practicas10Validas.get(i)+", ");
	out.print("</h3>");
	
	// M35 = Prácticas %s válidas
	out.print("<h3 class='bien'>"+String.format(Msgs.getMsg(Msgs.M35, lang), "P11")+"("+practicas11Validas.size()+"): ");
	for (int i=0; i<practicas11Validas.size(); i++)
		if (i == practicas11Validas.size()-1) out.print(practicas11Validas.get(i));
		else out.print(practicas11Validas.get(i)+", ");
	out.print("</h3>");
	
	// M35 = Prácticas %s válidas
	out.print("<h3 class='bien'>"+String.format(Msgs.getMsg(Msgs.M35, lang), "P12")+"("+practicas12Validas.size()+"): ");
	for (int i=0; i<practicas12Validas.size(); i++)
		if (i == practicas12Validas.size()-1) out.print(practicas12Validas.get(i));
		else out.print(practicas12Validas.get(i)+", ");
	out.print("</h3>");
	
	this.printBBC(out, lang, "", "");  // pie de la página	
}
	



	
	
	
	
	
	
	// piden una corrección. La práctica que hay que corregir viene en el parámetro practica 
	
	public void doGetCorrect (HttpServletRequest request, HttpServletResponse response, String lang, boolean esProfesor)
			throws IOException
	{
		String practica=null, cuenta=null, dni=null;
		
		PrintWriter out = response.getWriter();
		
		this.printHeader(out, lang);
		
		cuenta = request.getParameter("cuenta");
		
		if (cuenta == null) {	// no se recibe el numero de cuenta que hay que corregir
			// M11 = No se recibió número de cuenta
			this.sendError(out, Msgs.getMsg(Msgs.M11, lang), lang);
			return;
		}
		
		if (!esProfesor) {
			dni = request.getParameter("dni");
		
			if (dni == null) dni = "";
		
			boolean dniValido = Dnis.checkDni(Integer.parseInt(cuenta), dni);
		
			if (!dniValido) {
				// M29 = El NIF no se corresponde con la cuenta
				this.sendError(out, Msgs.getMsg(Msgs.M29, lang), lang);
				return;
			}
		}
		
		String rootFolder = PII+"pii"+cuenta+"/";
		File fd = new File (rootFolder);
		
		if (!fd.canRead()) {   // no se puede entrar en la cuenta a corregir
			// M12 = El directorio raíz de la cuenta no tiene permisos 750
			this.sendError(out, String.format(Msgs.getMsg(Msgs.M12, lang), cuenta), lang);
			return;
		}
		
		// M13 = Practicas
		String practicasFolder = rootFolder+Msgs.getMsg(Msgs.M13, lang);  // las prácticas están en el directorio 'Practicas'
		fd = new File (practicasFolder);
		
        if (!fd.canRead()) {  // no existe o no se puede entrar en el direcorio donde están las prácticas
        	// M14 = El directorio 'Practicas' no existe en la raíz de la cuenta pii"+cuenta+", o no tiene permisos para entrar"
        	this.sendError(out, String.format(Msgs.getMsg(Msgs.M14, lang), Msgs.getMsg(Msgs.M13, lang), cuenta), lang);
        	return;
        }
		
		practica = request.getParameter("practica");
		
		if (practica == null) { // no se recibe el número de la práctica que hay que corregir
			// M15 = No se indicó ninguna práctica para corregir
			this.sendError(out, Msgs.getMsg(Msgs.M15, lang), lang);
			return;
		}
		
		
		// Todo bien, por ahora, vamos a ver qué práctica hay que corregir

		try
		{	
			switch (practica) {
				case "p2":	this.doGetCorrectP2(out, cuenta, lang, esProfesor);
							break;
				case "p4":	this.doGetCorrectP4(out, cuenta, lang, esProfesor);
							break;
							
				case "p9":	this.doGetCorrectP9(out, cuenta, lang, esProfesor);
							break;
				case "p10":	this.doGetCorrectP10(out, cuenta, lang, esProfesor);
							break;
				case "p11":	this.doGetCorrectP11(out, cuenta, lang, esProfesor);
							break;
				case "p12":	this.doGetCorrectP12(out, cuenta, lang, esProfesor);
							break;
							
				// M16 = Solicitada la correción de una práctica desconocida
				default: throw(new Exception(Msgs.getMsg(Msgs.M16, lang)));
			}
								
		}
		catch(Exception e) {
			out.println("<span class='error'>Error: "+e.getMessage()+"</span><br>");
		}

		this.printBBC(out, lang, cuenta, dni);  // pie de la página
	}
	
	
	
	
	

	// ordena corregir P2 
	// devuelve 0 si pasa todos los tests
	
	public int doGetCorrectP2 (PrintWriter out, String cuenta, String lang, boolean esProfesor)
			throws Exception, IOException
	{
		final String P2_PKG_CLASS = "p2.ReadWriteClientsFile";
		final String P2_CLASS_FILE = "ReadWriteClientsFile.class";
		
		int resultado;
		List<String> command;
		Scanner input;
		String linea, logMessage = "";
		boolean hay_errors=false, hay_diffs=false;
		
		// M17 = Corrigiendo la tarea 2b de la P2 de pii
		out.println("<h3>"+Msgs.getMsg(Msgs.M17, lang)+cuenta+"</h3>");
		
		// M13 = Practicas
		String practicasFolder = PII+"pii"+cuenta+"/"+Msgs.getMsg(Msgs.M13, lang)+"/";
		String p2Folder = practicasFolder+"p2/";  // localización de la práctica del alumno
        
		File fd = new File (p2Folder);
        if (!fd.canRead()) {
        	// "El directorio 'Practicas/p2' de la cuenta no existe o no tiene permisos para entrar"));
            throw (new Exception(String.format(Msgs.getMsg(Msgs.M14, lang), Msgs.getMsg(Msgs.M13, lang)+"/p2", cuenta)));
        }
        
        String classFile = p2Folder+"/"+P2_CLASS_FILE; 	// práctica del alumno
        fd = new File (classFile);
        if (!fd.exists()) {
        	// M18 = En el directorio 'Practicas/p2' no está el fichero 
        	// M13 = Practicas
            throw (new Exception(String.format(Msgs.getMsg(Msgs.M18, lang), Msgs.getMsg(Msgs.M13, lang)+"/p2")+P2_CLASS_FILE));
        }
        
        
        // directorio de la cuenta de SINT donde están los ficheros de entrada y salida de la batería
        // directorio donde voy a guardar los resultados de la ejecución de los comandos (error y output)
        String wd_fileName = SINT0_PII_RT+"bt2/"; 
		String sources_fileName = wd_fileName+"sources/";
		String outputs_fileName = wd_fileName+"outputs/";  

		
		File wd_fd = new File(wd_fileName);
		if (!wd_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+wd_fileName+"'"));
        }
		
		File sources_fd = new File(sources_fileName);
		if (!sources_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+sources_fileName+"'"));
        }
		
		File outputs_fd = new File(outputs_fileName);
		if (!outputs_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+outputs_fileName+"'"));
        }
		
		
		String result_output_filename = outputs_fileName+cuenta+"_result_output";	// destino de los mensajes de output de rm, java y diff
		String result_error_filename = outputs_fileName+cuenta+"_result_error";	// destino de los mensajes de error de rm, java y diff
		File result_output_fd = new File(result_output_filename);
		File result_error_fd = new File(result_error_filename);

		
		// vamos a ejecutar los procesos de la corrección
		
		Process process;
		ProcessBuilder builder = new ProcessBuilder();
		builder.directory(wd_fd);					// directorio de trabajo de los comandos: java y diff  ("bt2")
		builder.redirectOutput(result_output_fd);	// output de los comandos: java y diff
		builder.redirectError(result_error_fd);		// error de los comandos: java y diff
		
		
		int numTests=3;		
		resultado = numTests;
		
		// Comienza la corrección
		
		String i;
		boolean timer;
		for (int x=1; x<=numTests; x++) {
			hay_errors=false; // errores de ejecución
			hay_diffs=false;  // 
			timer = false;

			i = String.format("%02d", x);
			String CLIENTS = "bt_d1a"+i+"_clients.txt";
			String GOOD = "bt_d1a"+i+"_output_good.txt";
			String OUTPUT = cuenta+"_bt_d1a"+i+"_output.txt";
			
			logMessage = cuenta+"; P2; T"+i;
	
			out.println("<hr><h3>"+Msgs.getMsg(Msgs.M07, lang)+" 1A  - Test "+i+"/"+numTests+"</h3>");
			
			
			// ofrecemos la consulta de los ficheros de input y los esperados a la salida
			// el primer pii es el prefijo, va al webapps, el segundo es la carpeta pii dentro del webapps
			
			out.println("<ul>");
			// M20 = Fichero inicial de clientes
			out.println("<li><a href='/pii/pii/bt2/sources/"+CLIENTS+"' target='_blank'>"+Msgs.getMsg(Msgs.M20, lang)+"</a><br>");
			// M21 = Fichero final de clientes esperado
			out.println("<li><a href='/pii/pii/bt2/sources/"+GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M21, lang)+"</a><br>");
			out.println("</ul>");
			
			// borramos los resultados previos
			// el working_dir es bt2 
			
			builder.command("rm", "-f", "outputs/"+OUTPUT, result_output_filename, result_error_filename);
			process = builder.start();  // ejecutamos el comando 'rm'
			process.waitFor();  // esperamos a que termine el comando 'rm'
			
			
			// suponemos que todo ha ido bien 
			
			// ejecutamos la práctica
			// el working_dir es bt2 
			
			command = new ArrayList<String>(Arrays.asList(JAVA, "-cp", practicasFolder, P2_PKG_CLASS, "sources/"+CLIENTS, "outputs/"+OUTPUT));
			builder.command(command);
			process = builder.start(); // ejecutamos la práctica
			timer = process.waitFor(2L, java.util.concurrent.TimeUnit.SECONDS);  // esperamos (1 seg max) a que termine la ejecución de la práctica
			
			// puede que haya errores en la ejecución de la práctica, aunque pueden ser sólo warnings
			
			if (!timer) {
				process.destroy();
                hay_errors = true;
                // M36 el programa no terminó tras 1 segundo
                out.println("<span class='error'>"+Msgs.getMsg(Msgs.M36, lang)+"</span><br>");  // presentamos el mensaje de error en pantalla           
            }

			
	        if (result_error_fd.length() > 0) {
	        		// el fichero de errores no está vacío
	        		input = new Scanner(result_error_fd);
	                while (input.hasNext()) { // leemos todas las líneas del fichero de errores
	                        linea = input.nextLine();
	                        if (linea.contains("NOTE: Picked up JDK_JAVA_OPTIONS")) continue;  // esta línea es un warning habitual, nos lo saltamos
	                        // es un error real
	                        if (!hay_errors) {  // si el flag de errores aun no está puesto, añadimos un mensaje y lo levantamos
	                        	// M22 = Errores en la ejecución de la práctica
	                            out.println("<b>"+Msgs.getMsg(Msgs.M22, lang)+"</b><br>");
	                            hay_errors = true;  // indicamos que hay hay realmente errores
	                        }
	                        out.println("<span class='error'>"+linea+"</span><br>");  // presentamos el mensaje de error en pantalla
	                }
	
	                input.close();
	        }
	        
	        
	        if (hay_errors) {
	        	logMessage = logMessage+"; "+ERROR_JAVA;  // ERROR_JAVA = 1, errores al ejecutar la práctica (p.e. NullPointerException)
	        	if (!esProfesor) logPIIChecker(logMessage);
	        	continue; // siguiente test
	        }
	        
	        // no hay errores en la ejecución de la práctica, vamos a ver las diferencias en el resultado
	        
	        // ejecutamos el comando 'diff'
			// el working_dir es bt2 
	        
			builder.command("diff", "-w", "outputs/"+OUTPUT, "sources/"+GOOD);
			process = builder.start();
			process.waitFor(); // esperamos a la finalización del 'diff'
			
			if (result_error_fd.length() > 0) { 
				// hubo errores en la ejecución del diff
				// M23 = Errores en la ejecución del diff
				out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
				input = new Scanner(result_error_fd);
	
				while (input.hasNext()) {  
					linea = input.nextLine();
					out.println("<span class='error'>"+linea+"</span><br>");
				}
				
				input.close();
				
				logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
				if (!esProfesor) logPIIChecker(logMessage);
	        	continue; // siguiente test
			}
			

			// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado

			// M24 = Diferencias respecto al resultado correcto
			out.println("<b>"+Msgs.getMsg(Msgs.M24, lang)+"</b><br>");
			out.println("******<br>");
	
			input = new Scanner(result_output_fd);
	
			while (input.hasNext()) {
				hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a false el flag de OK
				linea = input.nextLine();
				out.println("<span class='error'>"+linea+"</span><br>");
			}
			
			input.close();
			
			if (hay_diffs) {  // hay diffs
				logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
				// M26 = Fichero de salida que produce la práctica
				out.println("<a href='/pii/pii/bt2/outputs/"+OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
			}
			else {
				resultado--;  // este test fue bien
				logMessage = logMessage+"; "+TEST_OK;  // TEST_OK = 0, todo OK
				// M25 = NINGUNA DIFERENCIA: TODO BIEN
				out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
			}
			
			
			out.println("******<br>");
	        
	        if (!esProfesor) logPIIChecker(logMessage);
	        
		} // fin de los tests
		
		return resultado;  // debería ser 0 si todos los tests fueron bien
	}
	
	
	
	
	
	

	// ordena corregir P4 
	// devuelve 0 si pasa todos los tests
	
	public int doGetCorrectP4 (PrintWriter out, String cuenta, String lang, boolean esProfesor)
			throws Exception, IOException
	{
		final String P4_PKG_CLASS = "p4.P4";
		final String P4_CLASS_FILE = "P4.class";
		
		int resultado;
		List<String> command;
		Scanner input;
		String linea, logMessage = "";
		boolean hay_errors=false, hay_diffs=false;
		
		// M30 = Corrigiendo la actividad 4 de la P4 de pii
		out.println("<h3>"+Msgs.getMsg(Msgs.M30, lang)+cuenta+"</h3>");
		
		// M13 = Practicas
		String practicasFolder = PII+"pii"+cuenta+"/"+Msgs.getMsg(Msgs.M13, lang)+"/";
		String p4Folder = practicasFolder+"p4/";  // localización de la práctica del alumno
        
		File fd = new File (p4Folder);
        if (!fd.canRead()) {
        	// "El directorio 'Practicas/p4' de la cuenta no existe o no tiene permisos 755 para entrar"));
            throw (new Exception(String.format(Msgs.getMsg(Msgs.M14, lang), Msgs.getMsg(Msgs.M13, lang)+"/p4", cuenta)));
        }
        
        String classFile = p4Folder+"/"+P4_CLASS_FILE; 	// práctica del alumno
        fd = new File (classFile);
        if (!fd.exists()) {
        	// M18 = En el directorio 'Practicas/p4' no está el fichero 
            throw (new Exception(String.format(Msgs.getMsg(Msgs.M18, lang), Msgs.getMsg(Msgs.M13, lang)+"/p4")+P4_CLASS_FILE));
        }
        
        
        // directorio de la cuenta de SINT donde están los ficheros de entrada y salida de la batería
        // directorio donde voy a guardar los resultados de la ejecución de los comandos (error y output)
        String wd_fileName = SINT0_PII_RT+"bt4/"; 
		String sources_fileName = wd_fileName+"sources/";
		String outputs_fileName = wd_fileName+"outputs/";  

		
		File wd_fd = new File(wd_fileName);
		if (!wd_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+wd_fileName+"'"));
        }
		
		File sources_fd = new File(sources_fileName);
		if (!sources_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+sources_fileName+"'"));
        }
		
		File outputs_fd = new File(outputs_fileName);
		if (!outputs_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+outputs_fileName+"'"));
        }
		
		
		String result_rm_output_filename = outputs_fileName+cuenta+"_result_rm_output";	// destino de los mensajes de output de rm
		String result_rm_error_filename = outputs_fileName+cuenta+"_result_rm_error";	// destino de los mensajes de error de rm
		File result_rm_output_fd = new File(result_rm_output_filename);
		File result_rm_error_fd = new File(result_rm_error_filename);
		
		String result_java_output_filename_base = outputs_fileName+cuenta+"_result_java_output_";	// destino de los mensajes de output de java
		String result_java_error_filename = outputs_fileName+cuenta+"_result_java_error";	// destino de los mensajes de error de java
		// File result_java_output_fd = new File(result_java_output_filename);
		File result_java_error_fd = new File(result_java_error_filename);
		
		String result_diff_output_filename = outputs_fileName+cuenta+"_result_diff_output";	// destino de los mensajes de output de diff
		String result_diff_error_filename = outputs_fileName+cuenta+"_result_diff_error";	// destino de los mensajes de error de diff
		File result_diff_output_fd = new File(result_diff_output_filename);
		File result_diff_error_fd = new File(result_diff_error_filename);
		
		Process processRm;
		ProcessBuilder builderRm = new ProcessBuilder();
		builderRm.directory(wd_fd);					    // directorio de trabajo de los comandos rm  (bt9)
		builderRm.redirectOutput(result_rm_output_fd);	// output de los comandos rm
		builderRm.redirectError(result_rm_error_fd);	// error de los comandos rm
		
		Process processJava;
		ProcessBuilder builderJava = new ProcessBuilder();
		builderJava.directory(wd_fd);						// directorio de trabajo de los comandos java (bt9)
		// builderJava.redirectOutput(result_java_output_fd);	// output de los comandos java
		builderJava.redirectError(result_java_error_fd);	// error de los comandos java
		
		Process processDiff;
		ProcessBuilder builderDiff = new ProcessBuilder();
		builderDiff.directory(wd_fd);						// directorio de trabajo de los comandos diff (bt9)
		builderDiff.redirectOutput(result_diff_output_fd);	// output de los comandos diff
		builderDiff.redirectError(result_diff_error_fd);	// error de los comandos diff
		
		
		
		
		int numTests=7;	
		resultado = numTests;
		
		// Comienza la corrección
		
		String i;
		boolean timer;
		for (int x=1; x<=numTests; x++) {
			hay_errors= false;
			hay_diffs=false;
			timer=false;

			i = String.format("%02d", x);
			
			String result_java_output_filename = result_java_output_filename_base+i;
			File result_java_output_fd = new File(result_java_output_filename);
			builderJava.redirectOutput(result_java_output_fd);	// output de los comandos java
			
			String CLIENTS = "bt_d1b"+i+"_clients.txt";
			String HOTEL = "bt_d1b"+i+"_hotel.txt";
			String IOS = "bt_d1b"+i+"_ios.txt";
			String GOOD = "bt_d1b"+i+"_output_good.txt";
			
			String OUTPUT = cuenta+"_bt_d1b"+i+"_output.txt";
		
			logMessage = cuenta+"; P4; T"+i;
	
			// M07 = ENTREGABLE
			out.println("<hr><h3>"+Msgs.getMsg(Msgs.M07, lang)+" 1B  - Test "+i+"/"+numTests+"</h3>");
			
			
			// ofrecemos la consulta de los ficheros de input y los esperados a la salida
			// el primer pii es el prefijo, va al webapps, el segundo es la carpeta pii dentro del webapps
			
			out.println("<ul>");
			// M20 = Fichero inicial de clientes
			out.println("<li><a href='/pii/pii/bt4/sources/"+CLIENTS+"' target='_blank'>"+Msgs.getMsg(Msgs.M20, lang)+"</a><br>");
			// M31 = Fichero inicial del hotel
			out.println("<li><a href='/pii/pii/bt4/sources/"+HOTEL+"' target='_blank'>"+Msgs.getMsg(Msgs.M31, lang)+"</a><br>");
			// M32 = Fichero de entradas y salidas del hotel
			out.println("<li><a href='/pii/pii/bt4/sources/"+IOS+"' target='_blank'>"+Msgs.getMsg(Msgs.M32, lang)+"</a><br>");
			// M33 = Fichero final del hotel esperado
			out.println("<li><a href='/pii/pii/bt4/sources/"+GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M33, lang)+"</a><br>");
			out.println("</ul>");
			
			// borramos los resultados previos
			// el working_dir es bt4 
			
			builderRm.command("rm", "-f", "outputs/"+OUTPUT, result_rm_output_filename, result_rm_error_filename);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			
			// suponemos que todo ha ido bien 
			
			// ejecutamos la práctica
			// el working_dir es bt4 
			
			command = new ArrayList<String>(Arrays.asList(JAVA, "-cp", practicasFolder, P4_PKG_CLASS, "sources/"+HOTEL, "sources/"+IOS, "outputs/"+OUTPUT, "sources/"+CLIENTS));
			builderJava.command(command);
			processJava = builderJava.start(); // ejecutamos la práctica
			timer = processJava.waitFor(2L, java.util.concurrent.TimeUnit.SECONDS);  // esperamos a que termine la ejecución de la práctica
			
			// ponemos un enlace a los prints de la práctica
			
	        if (result_java_output_fd.length() > 0) 
        		// el fichero de output no está vacío
	        	out.println("<a href='/pii/pii/bt4/outputs/"+cuenta+"_result_java_output_"+i+"' target='_blank'>"+Msgs.getMsg(Msgs.M47, lang)+"</a><p>");
	        else
	        	// M48 = la práctica no ha generado ningún mensaje
	        	out.println(Msgs.getMsg(Msgs.M48, lang)+"<p>");
	        
	        
			// puede que haya errores en la ejecución de la práctica, aunque pueden ser sólo warnings
			
			if (!timer) {
				processJava.destroy();
                hay_errors = true;
                // M36 el programa no terminó tras 1 segundo
                out.println("<span class='error'>"+Msgs.getMsg(Msgs.M36, lang)+"</span><br>");  // presentamos el mensaje de error en pantalla           
            }
			
	        if (result_java_error_fd.length() > 0) {
	        		// el fichero de errores no está vacío
	        		input = new Scanner(result_java_error_fd);
	                while (input.hasNext()) { // leemos todas las líneas del fichero de errores
	                        linea = input.nextLine();
	                        if (linea.contains("NOTE: Picked up JDK_JAVA_OPTIONS")) continue;  // esta línea es un warning habitual, nos lo saltamos
	                        // es un error real
	                        if (!hay_errors) {  // si el flag de errores aun no está puesto, añadimos un mensaje y lo levantamos
	                        	// M22 = Errores en la ejecución de la práctica
	                            out.println("<b>"+Msgs.getMsg(Msgs.M22, lang)+"</b><br>");
	                            hay_errors = true;  // indicamos que hay hay realmente errores
	                        }
	                        out.println("<span class='error'>"+linea+"</span><br>");  // presentamos el mensaje de error en pantalla
	                }
	
	                input.close();
	        }
	        
	        if (hay_errors)  {
	        	logMessage = logMessage+"; "+ERROR_JAVA;  // ERROR_JAVA = 1, errores al ejecutar la práctica (p.e. NullPointerException)
	        	if (!esProfesor) logPIIChecker(logMessage);
	        	continue; // siguiente test
	        }
	        
        	// no hay errores en la ejecución de la práctica, vamos a ver las diferencias en el resultado
        
        	// ejecutamos el comando 'diff'
        	// el working_dir es bt4 
        
        	builderDiff.command("diff", "-w", "outputs/"+OUTPUT, "sources/"+GOOD);
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue; // siguiente test
        	}
        	
        	
		
        	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
		
        	// M24 = Diferencias respecto al resultado correcto
        	out.println("<b>"+Msgs.getMsg(Msgs.M24, lang)+"</b><br>");
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		// M26 = Fichero de salida que produce la práctica
        		out.println("<a href='/pii/pii/bt4/outputs/"+OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
        	}
        	else {
        		resultado--;  // este test fue bien
        		logMessage = logMessage+"; "+TEST_OK;  // TEST_OK = 0, todo OK
        		// M25 = NINGUNA DIFERENCIA: TODO BIEN
        		out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
        	}
        	
        	out.println("******<br>");       	
	        
	        if (!esProfesor) logPIIChecker(logMessage);
		}  // fin de los tests
		
		return resultado;  // debería ser 0 si todos los tests fueron bien
        
	}
	
	

	
	
	
	

	

	// ordena corregir P9
	// devuelve 0 si pasa todos los tests
	
	public int doGetCorrectP9 (PrintWriter out, String cuenta, String lang, boolean esProfesor)
			throws Exception, IOException
	{
		final String P9_PKG_CLASS = "p9.P9";
		final String P9_CLASS_FILE = "P9.class";
		File fd, fdMap;
		
		int resultado;
		List<String> command;
		Scanner input;
		String linea, logMessage = "";
		boolean hay_errors=false, hay_diffs=false;
		
		// M40 = Corrigiendo la P9 de 
		out.println("<h3>"+String.format(Msgs.getMsg(Msgs.M40, lang), "P9")+cuenta+"</h3>");
		
		// M13 = Practicas
		String practicasFolder = PII+"pii"+cuenta+"/"+Msgs.getMsg(Msgs.M13, lang)+"/";
		String p9Folder = practicasFolder+"p9/";  // localización de la práctica del alumno
        
		fd = new File (p9Folder);
        if (!fd.canRead()) {
        	// M14 = "El directorio 'Practicas/p9' de la cuenta no existe o no tiene permisos 755 para entrar"));
            throw (new Exception(String.format(Msgs.getMsg(Msgs.M14, lang), Msgs.getMsg(Msgs.M13, lang)+"/p9", cuenta)));
        }
        
        String classFile = p9Folder+"/"+P9_CLASS_FILE; 	// práctica del alumno
        fd = new File (classFile);
        if (!fd.exists()) {
        	// M18 = En el directorio 'Practicas/p9' no está el fichero 
            throw (new Exception(String.format(Msgs.getMsg(Msgs.M18, lang), Msgs.getMsg(Msgs.M13, lang)+"/p9")+P9_CLASS_FILE));
        }
        
        
        // directorio de la cuenta de SINT donde están los ficheros de entrada y salida de la batería
        // directorio donde voy a guardar los resultados de la ejecución de los comandos (error y output)
        String wd_fileName = SINT0_PII_RT+"bt9/"; 
		String sources_fileName = wd_fileName+"sources/";
		String outputs_fileName = wd_fileName+"outputs/";  

		
		File wd_fd = new File(wd_fileName);
		if (!wd_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+wd_fileName+"'"));
        }
		
		File sources_fd = new File(sources_fileName);
		if (!sources_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+sources_fileName+"'"));
        }
		
		File outputs_fd = new File(outputs_fileName);
		if (!outputs_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+outputs_fileName+"'"));
        }
		
		String result_rm_output_filename = outputs_fileName+cuenta+"_result_rm_output";	// destino de los mensajes de output de rm
		String result_rm_error_filename = outputs_fileName+cuenta+"_result_rm_error";	// destino de los mensajes de error de rm
		File result_rm_output_fd = new File(result_rm_output_filename);
		File result_rm_error_fd = new File(result_rm_error_filename);
		
		String result_java_output_filename_base = outputs_fileName+cuenta+"_result_java_output_";	// destino de los mensajes de output de java
		String result_java_error_filename = outputs_fileName+cuenta+"_result_java_error";	// destino de los mensajes de error de java
		// File result_java_output_fd = new File(result_java_output_filename);
		File result_java_error_fd = new File(result_java_error_filename);
		
		String result_diff_output_filename = outputs_fileName+cuenta+"_result_diff_output";	// destino de los mensajes de output de diff
		String result_diff_error_filename = outputs_fileName+cuenta+"_result_diff_error";	// destino de los mensajes de error de diff
		File result_diff_output_fd = new File(result_diff_output_filename);
		File result_diff_error_fd = new File(result_diff_error_filename);

		
		// vamos a ejecutar los procesos de la corrección
		
		Process processRm;
		ProcessBuilder builderRm = new ProcessBuilder();
		builderRm.directory(wd_fd);					    // directorio de trabajo de los comandos rm  (bt9)
		builderRm.redirectOutput(result_rm_output_fd);	// output de los comandos rm
		builderRm.redirectError(result_rm_error_fd);	// error de los comandos rm
		
		Process processJava;
		ProcessBuilder builderJava = new ProcessBuilder();
		builderJava.directory(wd_fd);						// directorio de trabajo de los comandos java (bt9)
		// builderJava.redirectOutput(result_java_output_fd);	// output de los comandos java
		builderJava.redirectError(result_java_error_fd);	// error de los comandos java
		
		Process processDiff;
		ProcessBuilder builderDiff = new ProcessBuilder();
		builderDiff.directory(wd_fd);						// directorio de trabajo de los comandos diff (bt9)
		builderDiff.redirectOutput(result_diff_output_fd);	// output de los comandos diff
		builderDiff.redirectError(result_diff_error_fd);	// error de los comandos diff
		
		
		int numTests=5;	
		resultado = numTests;
		
		// Comienza la corrección
		
		String i;
		boolean timer;
		for (int x=1; x<=numTests; x++) {
			hay_errors= false;
			hay_diffs=false;
			timer=false;

			i = String.format("%02d", x);
			
			String result_java_output_filename = result_java_output_filename_base+i;
			File result_java_output_fd = new File(result_java_output_filename);
			builderJava.redirectOutput(result_java_output_fd);	// output de los comandos java

			
			String HOTEL = "bt_d2_"+i+"_hotel.txt";
			String IOS = "bt_d2_"+i+"_ios.txt";
			String HOTEL_OUTPUT = cuenta+"_bt_d2_"+i+"_hotel_output.txt";
			String CLIENTS = "bt_d2_"+i+"_clients.txt";
			String CLIENTS_OUTPUT = cuenta+"_bt_d2_"+i+"_clients_output.txt";
			String HOTEL_MAP_OUTPUT = cuenta+"_bt_d2_"+i+"_hotel_map_output.txt";
			
			String HOTEL_OUTPUT_GOOD = "bt_d2_"+i+"_hotel_output_good.txt";
			String CLIENTS_OUTPUT_GOOD = "bt_d2_"+i+"_clients_output_good.txt";
			String HOTEL_MAP_GOOD = "bt_d2_"+i+"_hotel_map_good.txt";
			
		
			logMessage = cuenta+"; P9; T"+i;
	
			// M07 = ENTREGABLE
			out.println("<hr><h3>"+Msgs.getMsg(Msgs.M07, lang)+" 2  - Test "+i+"/"+numTests+"</h3>");
			
			
			// ofrecemos la consulta de los ficheros de input y los esperados a la salida
			// el primer pii es el prefijo, va al webapps, el segundo es la carpeta pii dentro del webapps
			
			out.println("<ul>");
			// M31 = Fichero inicial del hotel
			out.println("<li><a href='/pii/pii/bt9/sources/"+HOTEL+"' target='_blank'>"+Msgs.getMsg(Msgs.M31, lang)+"</a><br>");
			
			// M32 = Fichero de entradas y salidas del hotel
			out.println("<li><a href='/pii/pii/bt9/sources/"+IOS+"' target='_blank'>"+Msgs.getMsg(Msgs.M32, lang)+"</a><br>");
			
			// M20 = Fichero inicial de clientes
			out.println("<li><a href='/pii/pii/bt9/sources/"+CLIENTS+"' target='_blank'>"+Msgs.getMsg(Msgs.M20, lang)+"</a><br>");
			
			// M33 = Fichero final del hotel esperado
			out.println("<li><a href='/pii/pii/bt9/sources/"+HOTEL_OUTPUT_GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M33, lang)+"</a><br>");
			
			// M41 = Fichero final de clientes esperado
			out.println("<li><a href='/pii/pii/bt9/sources/"+CLIENTS_OUTPUT_GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M41, lang)+"</a><br>");

			// M42 = Fichero con el mapa final del hotel esperado
			out.println("<li><a href='/pii/pii/bt9/sources/"+HOTEL_MAP_GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M42, lang)+"</a><br>");
			
			out.println("</ul>");
			
			// borramos los resultados previos
			// el working_dir es bt9
			
			builderRm.command("rm", "-f", "outputs/"+HOTEL_OUTPUT);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			builderRm.command("rm", "-f", "outputs/"+CLIENTS_OUTPUT);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			builderRm.command("rm", "-f", "outputs/"+HOTEL_MAP_OUTPUT);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			builderRm.command("rm", "-f", result_rm_output_filename, result_rm_error_filename, result_java_output_filename, result_java_error_filename, result_diff_output_filename, result_diff_error_filename);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			
			// suponemos que todo ha ido bien 
			
			// ejecutamos la práctica
			// el working_dir es bt9 
			
			command = new ArrayList<String>(Arrays.asList(JAVA, "-cp", practicasFolder, P9_PKG_CLASS, "sources/"+HOTEL, "sources/"+IOS, "outputs/"+HOTEL_OUTPUT, "sources/"+CLIENTS, "outputs/"+CLIENTS_OUTPUT, "outputs/"+HOTEL_MAP_OUTPUT));
			builderJava.command(command);
			processJava = builderJava.start(); // ejecutamos la práctica
			timer = processJava.waitFor(2L, java.util.concurrent.TimeUnit.SECONDS);  // esperamos a que termine la ejecución de la práctica
			
			// ponemos un enlace a los prints de la práctica
			
	        if (result_java_output_fd.length() > 0) 
        		// el fichero de output no está vacío
	        	out.println("<a href='/pii/pii/bt9/outputs/"+cuenta+"_result_java_output_"+i+"' target='_blank'>"+Msgs.getMsg(Msgs.M47, lang)+"</a><p>");
	        else
	        	// M48 = la práctica no ha generado ningún mensaje
	        	out.println(Msgs.getMsg(Msgs.M48, lang)+"<p>");
			
			
			
			// puede que haya errores en la ejecución de la práctica, aunque pueden ser sólo warnings
			
			if (!timer) {
				processJava.destroy();
                hay_errors = true;
                // M36 el programa no terminó tras 1 segundo
                out.println("<span class='error'>"+Msgs.getMsg(Msgs.M36, lang)+"</span><br>");  // presentamos el mensaje de error en pantalla           
            }
			
	        if (result_java_error_fd.length() > 0) {
	        		// el fichero de errores no está vacío
	        		input = new Scanner(result_java_error_fd);
	                while (input.hasNext()) { // leemos todas las líneas del fichero de errores
	                        linea = input.nextLine();
	                        if (linea.contains("NOTE: Picked up JDK_JAVA_OPTIONS")) continue;  // esta línea es un warning habitual, nos lo saltamos
	                        // es un error real
	                        if (!hay_errors) {  // si el flag de errores aun no está puesto, añadimos un mensaje y lo levantamos
	                        	// M22 = Errores en la ejecución de la práctica
	                            out.println("<b>"+Msgs.getMsg(Msgs.M22, lang)+"</b><br>");
	                            hay_errors = true;  // indicamos que hay hay realmente errores
	                        }
	                        out.println("<span class='error'>"+linea+"</span><br>");  // presentamos el mensaje de error en pantalla
	                }
	
	                input.close();
	        }
	        
	        if (hay_errors)  {
	        	logMessage = logMessage+"; "+ERROR_JAVA;  // ERROR_JAVA = 1, errores al ejecutar la práctica (p.e. NullPointerException)
	        	if (!esProfesor) logPIIChecker(logMessage);
	        	continue; // siguiente test
	        }
	        
        	// no hay errores en la ejecución de la práctica, vamos a ver las diferencias en los resultados
        

	        
	        
	        
	        // Comprobación 1: ver diferencias con el hotel final
			
	        // M43 = Comprobando diferencias en el fichero final del hotel
			out.println("<b>1/3. "+Msgs.getMsg(Msgs.M43, lang)+"</b><br>");
			
        	// ejecutamos el comando 'diff'
        	// el working_dir es bt9
        
        	builderDiff.command("diff", "-w", "outputs/"+HOTEL_OUTPUT, "sources/"+HOTEL_OUTPUT_GOOD);
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;   // si falla una cosa, cortamos el test
        	}

        	
         	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
    		
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		// M26 = Fichero de salida que produce la práctica
        		out.println("<a href='/pii/pii/bt9/outputs/"+HOTEL_OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;  // si falla una cosa, cortamos el test
        	}

        	// M25 = NINGUNA DIFERENCIA: TODO BIEN
        	out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
        	out.println("******<br>");    

        	
        	
        	
        	
        	// Comprobación 2: ver diferencias con el fichero de clientes final
        	
        	out.println("<b>2/3. "+Msgs.getMsg(Msgs.M44, lang)+"</b><br>");
			
        	// ejecutamos el comando 'diff'
        	// el working_dir es bt9
        	
        	builderDiff.command("diff", "-w", "outputs/"+CLIENTS_OUTPUT, "sources/"+CLIENTS_OUTPUT_GOOD);
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue; // siguiente test
        	}

            	
         	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
    		
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		// M26 = Fichero de salida que produce la práctica
        		out.println("<a href='/pii/pii/bt9/outputs/"+CLIENTS_OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;  // si falla una cosa, cortamos el test
        	}

        	// M25 = NINGUNA DIFERENCIA: TODO BIEN
        	out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
        	out.println("******<br>");    

        	
        	
        	
        	
        	
           	// Comprobación 3:  ver diferencias con el mapa del hotel
        	out.println("<b>3/3. "+Msgs.getMsg(Msgs.M45, lang)+"</b><br>");
			
        	// el working_dir es bt9
        	
        	// vemos si existe el fichero de mapa (opcional). Si existe, debe estar bien
        	
        	fdMap = new File (builderJava.directory().getAbsolutePath()+"/outputs/"+HOTEL_MAP_OUTPUT);  // bt9
        	
        	if (!fdMap.exists()) {
        		// M46 No hay fichero de mapa
        		out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M46, lang)+"</span><br>");
        		resultado--;  // si no hay mapa, este test fue bien
        		logMessage = logMessage+"; "+TEST_OK;  // TEST_OK = 0, todo OK
        		logPIIChecker(logMessage);
        		continue;
        	}
        	
        	// ejecutamos el comando 'diff'
        	
        	builderDiff.command("diff", "-w", "outputs/"+HOTEL_MAP_OUTPUT, "sources/"+HOTEL_MAP_GOOD);
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;
        	}
            	
         	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
    		
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		// M26 = Fichero de salida que produce la práctica
        		out.println("<a href='/pii/pii/bt9/outputs/"+HOTEL_MAP_OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;  // si falla una cosa, cortamos el test
        	}

    		resultado--;  // el mapa está bien, este test fue bien
    		logMessage = logMessage+"; "+TEST_OK;  // TEST_OK = 0, todo OK
    		// M25 = NINGUNA DIFERENCIA: TODO BIEN
    		out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");

        	
        	out.println("******<br>");    
        	if (!esProfesor) logPIIChecker(logMessage);
    		// se acabó este test
        	
		}  // fin de los tests
		
		return resultado;  // debería ser 0 si todos los tests fueron bien
        
	}
	
	
	
	
	
	
	
	
	
	
	
	// ordena corregir P10
	// devuelve 0 si pasa todos los tests
	
	public int doGetCorrectP10 (PrintWriter out, String cuenta, String lang, boolean esProfesor)
			throws Exception, IOException
	{
		final String P10_PKG_CLASS = "e3.p10.P10";
		final String P10_CLASS_FILE = "P10.class";
		File fd, fdMap;
		
		int resultado;
		List<String> command;
		Scanner input;
		String linea, logMessage = "";
		boolean hay_errors=false, hay_diffs=false;
		
		// M40 = Corrigiendo la P10 de 
		out.println("<h3>"+String.format(Msgs.getMsg(Msgs.M40, lang), "P10")+cuenta+"</h3>");
		
		// M13 = Practicas
		String practicasFolder = PII+"pii"+cuenta+"/"+Msgs.getMsg(Msgs.M13, lang)+"/";
		String p10Folder = practicasFolder+"e3/p10/";  // localización de la práctica del alumno
        
		fd = new File (p10Folder);
        if (!fd.canRead()) {
        	// M14 = "El directorio 'Practicas/e3/p10' de la cuenta no existe o no tiene permisos 755 para entrar"));
            throw (new Exception(String.format(Msgs.getMsg(Msgs.M14, lang), Msgs.getMsg(Msgs.M13, lang)+"/e3/p10", cuenta)));
        }
        
        String classFile = p10Folder+"/"+P10_CLASS_FILE; 	// práctica del alumno
        fd = new File (classFile);
        if (!fd.exists()) {
        	// M18 = En el directorio 'Practicas/e3/p10' no está el fichero 
            throw (new Exception(String.format(Msgs.getMsg(Msgs.M18, lang), Msgs.getMsg(Msgs.M13, lang)+"/e3/p10")+P10_CLASS_FILE));
        }
        
        
        // directorio de la cuenta de SINT donde están los ficheros de entrada y salida de la batería
        // directorio donde voy a guardar los resultados de la ejecución de los comandos (error y output)
        String wd_fileName = SINT0_PII_RT+"bt10/"; 
		String sources_fileName = wd_fileName+"sources/";
		String outputs_fileName = wd_fileName+"outputs/";  

		
		File wd_fd = new File(wd_fileName);
		if (!wd_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+wd_fileName+"'"));
        }
		
		File sources_fd = new File(sources_fileName);
		if (!sources_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+sources_fileName+"'"));
        }
		
		File outputs_fd = new File(outputs_fileName);
		if (!outputs_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+outputs_fileName+"'"));
        }
		
		String result_rm_output_filename = outputs_fileName+cuenta+"_result_rm_output";	// destino de los mensajes de output de rm
		String result_rm_error_filename = outputs_fileName+cuenta+"_result_rm_error";	// destino de los mensajes de error de rm
		File result_rm_output_fd = new File(result_rm_output_filename);
		File result_rm_error_fd = new File(result_rm_error_filename);
		
		String result_java_output_filename_base = outputs_fileName+cuenta+"_result_java_output_";	// destino de los mensajes de output de java
		String result_java_error_filename = outputs_fileName+cuenta+"_result_java_error";	// destino de los mensajes de error de java
		// File result_java_output_fd = new File(result_java_output_filename);
		File result_java_error_fd = new File(result_java_error_filename);
		
		String result_diff_output_filename = outputs_fileName+cuenta+"_result_diff_output";	// destino de los mensajes de output de diff
		String result_diff_error_filename = outputs_fileName+cuenta+"_result_diff_error";	// destino de los mensajes de error de diff
		File result_diff_output_fd = new File(result_diff_output_filename);
		File result_diff_error_fd = new File(result_diff_error_filename);

		
		// vamos a ejecutar los procesos de la corrección
		
		Process processRm;
		ProcessBuilder builderRm = new ProcessBuilder();
		builderRm.directory(wd_fd);					    // directorio de trabajo de los comandos rm  (bt10)
		builderRm.redirectOutput(result_rm_output_fd);	// output de los comandos rm
		builderRm.redirectError(result_rm_error_fd);	// error de los comandos rm
		
		Process processJava;
		ProcessBuilder builderJava = new ProcessBuilder();
		builderJava.directory(wd_fd);						// directorio de trabajo de los comandos java (bt10)
		// builderJava.redirectOutput(result_java_output_fd);	// output de los comandos java
		builderJava.redirectError(result_java_error_fd);	// error de los comandos java
		
		Process processDiff;
		ProcessBuilder builderDiff = new ProcessBuilder();
		builderDiff.directory(wd_fd);						// directorio de trabajo de los comandos diff (bt10)
		builderDiff.redirectOutput(result_diff_output_fd);	// output de los comandos diff
		builderDiff.redirectError(result_diff_error_fd);	// error de los comandos diff
		
		
		int numTests=1;	
		resultado = numTests;
		
		// Comienza la corrección
		
		String i;
		boolean timer;
		for (int x=1; x<=numTests; x++) {
			hay_errors= false;
			hay_diffs=false;
			timer=false;

			i = String.format("%02d", x);
			
			String result_java_output_filename = result_java_output_filename_base+i;
			File result_java_output_fd = new File(result_java_output_filename);
			builderJava.redirectOutput(result_java_output_fd);	// output de los comandos java
			
			String HOTEL = "bt_d3a_"+i+"_hotel.txt";
			String IOS = "bt_d3a_"+i+"_ios.txt";
			
			String HOTEL_OUTPUT = cuenta+"_bt_d3a_"+i+"_hotel_output.txt";
			String HOTEL_OUTPUT_GOOD = "bt_d3a_"+i+"_hotel_output_good.txt";
			
			String CLIENTS = "bt_d3a_"+i+"_clients.txt";
			String CLIENTS_OUTPUT = cuenta+"_bt_d3a_"+i+"_clients_output.txt";
			String CLIENTS_OUTPUT_GOOD = "bt_d3a_"+i+"_clients_output_good.txt";
			
			String CLIENT_OBJS_OUTPUT = cuenta+"_bt_d3a_"+i+"_client_objs_output.dat";
			String CLIENT_OBJS_GOOD = "bt_d3a_"+i+"_client_objs_good.dat";
			
			String CLIENT_POINTS_OUTPUT = cuenta+"_bt_d3a_"+i+"_client_points_output.dat";
			String CLIENT_POINTS_GOOD = "bt_d3a_"+i+"_client_points_good.dat";
			
			
			
			logMessage = cuenta+"; P10; T"+i;
	
			// M07 = ENTREGABLE
			out.println("<hr><h3>"+Msgs.getMsg(Msgs.M07, lang)+" 3  - Test "+i+"/"+numTests+"</h3>");
			
			
			// ofrecemos la consulta de los ficheros de input y los esperados a la salida
			// el primer pii es el prefijo, va al webapps, el segundo es la carpeta pii dentro del webapps
			
			out.println("<ul>");
			// M31 = Fichero inicial del hotel
			out.println("<li><a href='/pii/pii/bt10/sources/"+HOTEL+"' target='_blank'>"+Msgs.getMsg(Msgs.M31, lang)+"</a><br>");
			
			// M32 = Fichero de entradas y salidas del hotel
			out.println("<li><a href='/pii/pii/bt10/sources/"+IOS+"' target='_blank'>"+Msgs.getMsg(Msgs.M32, lang)+"</a><br>");
			
			// M33 = Fichero final del hotel esperado
			out.println("<li><a href='/pii/pii/bt10/sources/"+HOTEL_OUTPUT_GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M33, lang)+"</a><br>");
			
			// M20 = Fichero inicial de clientes
			out.println("<li><a href='/pii/pii/bt10/sources/"+CLIENTS+"' target='_blank'>"+Msgs.getMsg(Msgs.M20, lang)+"</a><br>");
			
			// M41 = Fichero final de texto de clientes esperado
			out.println("<li><a href='/pii/pii/bt10/sources/"+CLIENTS_OUTPUT_GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M41, lang)+"</a><br>");
			
			// M56 = Fichero final binario de clientes esperado
			out.println("<li><a href='/pii/pii/bt10/sources/"+CLIENT_OBJS_GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M56, lang)+"</a><br>");
			
			// M57 = Fichero final binario de puntos esperado
			out.println("<li><a href='/pii/pii/bt10/sources/"+CLIENT_POINTS_GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M57, lang)+"</a><br>");
			
			out.println("</ul>");
			
			// borramos los resultados previos
			// el working_dir es bt10
			
			builderRm.command("rm", "-f", "outputs/"+HOTEL_OUTPUT);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			builderRm.command("rm", "-f", "outputs/"+CLIENTS_OUTPUT);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			builderRm.command("rm", "-f", "outputs/"+CLIENT_OBJS_OUTPUT);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'

			builderRm.command("rm", "-f", "outputs/"+CLIENT_POINTS_OUTPUT);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			builderRm.command("rm", "-f", result_rm_output_filename, result_rm_error_filename, result_java_output_filename, result_java_error_filename, result_diff_output_filename, result_diff_error_filename);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			// suponemos que todo ha ido bien 
			
			// ejecutamos la práctica
			// el working_dir es bt10 
			
			command = new ArrayList<String>(Arrays.asList(JAVA, "-cp", practicasFolder, P10_PKG_CLASS, "sources/"+HOTEL, "sources/"+IOS, "outputs/"+HOTEL_OUTPUT, "sources/"+CLIENTS, "outputs/"+CLIENTS_OUTPUT, "outputs/"+CLIENT_OBJS_OUTPUT, "outputs/"+CLIENT_POINTS_OUTPUT));
			builderJava.command(command);
			processJava = builderJava.start(); // ejecutamos la práctica
			timer = processJava.waitFor(2L, java.util.concurrent.TimeUnit.SECONDS);  // esperamos a que termine la ejecución de la práctica
			
			// ponemos un enlace a los prints de la práctica
			
	        if (result_java_output_fd.length() > 0) {
        		// el fichero de output no está vacío
	        	out.println("<a href='/pii/pii/bt10/outputs/"+cuenta+"_result_java_output_"+i+"' target='_blank'>"+Msgs.getMsg(Msgs.M47, lang)+"</a><p>");
	        }
	        else
	        	// M48 = la práctica no ha generado ningún mensaje
	        	out.println(Msgs.getMsg(Msgs.M48, lang)+"<p>");
			
			
			
			
			// puede que haya errores en la ejecución de la práctica, aunque pueden ser sólo warnings
			
			if (!timer) {
				processJava.destroy();
                hay_errors = true;
                // M36 el programa no terminó tras 1 segundo
                out.println("<span class='error'>"+Msgs.getMsg(Msgs.M36, lang)+"</span><br>");  // presentamos el mensaje de error en pantalla           
            }
			
	        if (result_java_error_fd.length() > 0) {
	        		// el fichero de errores no está vacío
	        		input = new Scanner(result_java_error_fd);
	                while (input.hasNext()) { // leemos todas las líneas del fichero de errores
	                        linea = input.nextLine();
	                        if (linea.contains("NOTE: Picked up JDK_JAVA_OPTIONS")) continue;  // esta línea es un warning habitual, nos lo saltamos
	                        // es un error real
	                        if (!hay_errors) {  // si el flag de errores aun no está puesto, añadimos un mensaje y lo levantamos
	                        	// M22 = Errores en la ejecución de la práctica
	                            out.println("<b>"+Msgs.getMsg(Msgs.M22, lang)+"</b><br>");
	                            hay_errors = true;  // indicamos que hay hay realmente errores
	                        }
	                        out.println("<span class='error'>"+linea+"</span><br>");  // presentamos el mensaje de error en pantalla
	                }
	
	                input.close();
	        }
	        
	        if (hay_errors)  {
	        	logMessage = logMessage+"; "+ERROR_JAVA;  // ERROR_JAVA = 1, errores al ejecutar la práctica (p.e. NullPointerException)
	        	if (!esProfesor) logPIIChecker(logMessage);
	        	continue; // siguiente test
	        }
	        
        	// no hay errores en la ejecución de la práctica, vamos a ver las diferencias en los resultados
        

	        
	        
	        
	        // Comprobación 1: ver diferencias con el hotel final
			
	        // M43 = Comprobando diferencias en el fichero final del hotel
			out.println("<b>1/4. "+Msgs.getMsg(Msgs.M43, lang)+"</b><br>");
			
        	// ejecutamos el comando 'diff'
        	// el working_dir es bt10
        
        	builderDiff.command("diff", "-w", "outputs/"+HOTEL_OUTPUT, "sources/"+HOTEL_OUTPUT_GOOD);
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		
        		continue;   // si falla una cosa, cortamos el test
        	}

        	
         	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
    		
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		// M26 = Fichero de salida que produce la práctica
        		out.println("<a href='/pii/pii/bt10/outputs/"+HOTEL_OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;  // si falla una cosa, cortamos el test
        	}

        	// M25 = NINGUNA DIFERENCIA: TODO BIEN
        	out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
        	out.println("******<br>");    

        	
        	
        	
        	
        	
        	// Comprobación 2: ver diferencias con el fichero de clientes final
        	
        	// M44 = Comprobando diferencias en el fichero final de clientes
        	out.println("<b>2/4. "+Msgs.getMsg(Msgs.M44, lang)+"</b><br>");
			
        	// ejecutamos el comando 'diff'
        	// el working_dir es bt10
        	
        	builderDiff.command("diff", "-w", "outputs/"+CLIENTS_OUTPUT, "sources/"+CLIENTS_OUTPUT_GOOD);
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue; // siguiente test
        	}

            	
         	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
    		
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		// M26 = Fichero de salida que produce la práctica
        		out.println("<a href='/pii/pii/bt10/outputs/"+CLIENTS_OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;  // si falla una cosa, cortamos el test
        	}
        	
        	// M25 = NINGUNA DIFERENCIA: TODO BIEN
        	out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
        	out.println("******<br>");    

        	
        	
        	
        	
        	
           	// Comprobación 3: ver diferencias con el fichero binario de clientes
        	
        	// M50 = Comprobando diferencias en el fichero binario de clientes
        	out.println("<b>3/4. "+Msgs.getMsg(Msgs.M50, lang)+"</b><br>");
			
        	// ejecutamos el comando 'diff'
        	// el working_dir es bt10
        	
        	builderDiff.command("diff", "-w", "outputs/"+CLIENT_OBJS_OUTPUT, "sources/"+CLIENT_OBJS_GOOD);
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;
        	}

            	
         	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
    		
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		
        		// M26 = Fichero de salida que produce la práctica
        		out.println("<a href='/pii/pii/bt10/outputs/"+CLIENT_OBJS_OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;  // si falla una cosa, cortamos el test
        	}

        	
    		// M25 = NINGUNA DIFERENCIA: TODO BIEN
    		out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
        	out.println("******<br>");    
        	
        	
        	
        	
 
           	// Comprobación 4: ver diferencias con el fichero binario de puntos
        	
        	// M51 = Comprobando diferencias en el fichero binario de puntos
        	out.println("<b>4/4. "+Msgs.getMsg(Msgs.M51, lang)+"</b><br>");
			
        	// ejecutamos el comando 'diff'
        	// el working_dir es bt10
        	
        	builderDiff.command("diff", "-w", "outputs/"+CLIENT_POINTS_OUTPUT, "sources/"+CLIENT_POINTS_GOOD);
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;
        	}

            	
         	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
    		
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		
        		// M26 = Fichero de salida que produce la práctica
        		out.println("<a href='/pii/pii/bt10/outputs/"+CLIENT_POINTS_OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;  // si falla una cosa, cortamos el test
        	}

        	
    		// M25 = NINGUNA DIFERENCIA: TODO BIEN
    		out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
        	out.println("******<br>");    
        	
        	
        	
        	
    		resultado--;  // este test fue bien
    		logMessage = logMessage+"; "+TEST_OK;  // TEST_OK = 0, todo OK
        	
        	
        	if (!esProfesor) logPIIChecker(logMessage);
        	
		}  // fin de los tests
		
		return resultado;  // debería ser 0 si todos los tests fueron bien
        
	}
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	// ordena corregir P11
	// devuelve 0 si pasa todos los tests
	
	public int doGetCorrectP11 (PrintWriter out, String cuenta, String lang, boolean esProfesor)
			throws Exception, IOException
	{
		final String P11_PKG_CLASS = "e3.p11.P11";
		final String P11_CLASS_FILE = "P11.class";
		File fd, fdMap;
		
		int resultado;
		List<String> command;
		Scanner input;
		String linea, logMessage = "";
		boolean hay_errors=false, hay_diffs=false;
		
		// M40 = Corrigiendo la P11 de 
		out.println("<h3>"+String.format(Msgs.getMsg(Msgs.M40, lang), "P11")+cuenta+"</h3>");
		
		// M13 = Practicas
		String practicasFolder = PII+"pii"+cuenta+"/"+Msgs.getMsg(Msgs.M13, lang)+"/";
		String p11Folder = practicasFolder+"e3/p11/";  // localización de la práctica del alumno
        
		fd = new File (p11Folder);
        if (!fd.canRead()) {
        	// M14 = "El directorio 'Practicas/e3/p11' de la cuenta no existe o no tiene permisos 755 para entrar"));
            throw (new Exception(String.format(Msgs.getMsg(Msgs.M14, lang), Msgs.getMsg(Msgs.M13, lang)+"/e3/p11", cuenta)));
        }
        
        String classFile = p11Folder+"/"+P11_CLASS_FILE; 	// práctica del alumno
        fd = new File (classFile);
        if (!fd.exists()) {
        	// M18 = En el directorio 'Practicas/e3/p11' no está el fichero 
            throw (new Exception(String.format(Msgs.getMsg(Msgs.M18, lang), Msgs.getMsg(Msgs.M13, lang)+"/e3/p11")+P11_CLASS_FILE));
        }
        
        
        // directorio de la cuenta de SINT donde están los ficheros de entrada y salida de la batería
        // directorio donde voy a guardar los resultados de la ejecución de los comandos (error y output)
        String wd_fileName = SINT0_PII_RT+"bt11/"; 
		String sources_fileName = wd_fileName+"sources/";
		String outputs_fileName = wd_fileName+"outputs/";  

		
		File wd_fd = new File(wd_fileName);
		if (!wd_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+wd_fileName+"'"));
        }
		
		File sources_fd = new File(sources_fileName);
		if (!sources_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+sources_fileName+"'"));
        }
		
		File outputs_fd = new File(outputs_fileName);
		if (!outputs_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+outputs_fileName+"'"));
        }
		
		String result_rm_output_filename = outputs_fileName+cuenta+"_result_rm_output";	// destino de los mensajes de output de rm
		String result_rm_error_filename = outputs_fileName+cuenta+"_result_rm_error";	// destino de los mensajes de error de rm
		File result_rm_output_fd = new File(result_rm_output_filename);
		File result_rm_error_fd = new File(result_rm_error_filename);
		
		String result_java_output_filename_base = outputs_fileName+cuenta+"_result_java_output_";	// destino de los mensajes de output de java
		String result_java_error_filename = outputs_fileName+cuenta+"_result_java_error";	// destino de los mensajes de error de java
		// lo creo uno a uno en cada test
		// File result_java_output_fd = new File(result_java_output_filename);
		File result_java_error_fd = new File(result_java_error_filename);
		
		String result_diff_output_filename = outputs_fileName+cuenta+"_result_diff_output";	// destino de los mensajes de output de diff
		String result_diff_error_filename = outputs_fileName+cuenta+"_result_diff_error";	// destino de los mensajes de error de diff
		File result_diff_output_fd = new File(result_diff_output_filename);
		File result_diff_error_fd = new File(result_diff_error_filename);

		
		// vamos a ejecutar los procesos de la corrección
		
		Process processRm;
		ProcessBuilder builderRm = new ProcessBuilder();
		builderRm.directory(wd_fd);					    // directorio de trabajo de los comandos rm  (bt10)
		builderRm.redirectOutput(result_rm_output_fd);	// output de los comandos rm
		builderRm.redirectError(result_rm_error_fd);	// error de los comandos rm
		
		Process processJava;
		ProcessBuilder builderJava = new ProcessBuilder();
		builderJava.directory(wd_fd);						// directorio de trabajo de los comandos java (bt10)
		// lo redirijo uno a uno en cada test
		// builderJava.redirectOutput(result_java_output_fd);	// output de los comandos java
		builderJava.redirectError(result_java_error_fd);	// error de los comandos java
		
		Process processDiff;
		ProcessBuilder builderDiff = new ProcessBuilder();
		builderDiff.directory(wd_fd);						// directorio de trabajo de los comandos diff (bt10)
		builderDiff.redirectOutput(result_diff_output_fd);	// output de los comandos diff
		builderDiff.redirectError(result_diff_error_fd);	// error de los comandos diff
		
		
		int numTests=1;	
		resultado = numTests;
		
		// Comienza la corrección
		
		String i;
		boolean timer;
		for (int x=1; x<=numTests; x++) {
			hay_errors= false;
			hay_diffs=false;
			timer=false;

			i = String.format("%02d", x);
			
			String result_java_output_filename = result_java_output_filename_base+i;
			File result_java_output_fd = new File(result_java_output_filename);
			builderJava.redirectOutput(result_java_output_fd);	// output de los comandos java
			
			
			
			String HOTEL = "bt_d3b_"+i+"_hotel.txt";
			String IOS = "bt_d3b_"+i+"_ios.txt";
			
			String HOTEL_OUTPUT = cuenta+"_bt_d3b_"+i+"_hotel_output.txt";
			String HOTEL_OUTPUT_GOOD = "bt_d3b_"+i+"_hotel_output_good.txt";
			
			String CLIENTS = "bt_d3b_"+i+"_clients.txt";
			String CLIENTS_OUTPUT = cuenta+"_bt_d3b_"+i+"_clients_output.txt";
			String CLIENTS_OUTPUT_GOOD = "bt_d3b_"+i+"_clients_output_good.txt";
			
			
			String CLIENT_OBJS_OUTPUT = cuenta+"_bt_d3b_"+i+"_client_objs_output.dat";
			String CLIENT_OBJS_GOOD = "bt_d3b_"+i+"_client_objs_good.dat";
			
			String CLIENT_POINTS_OBJ_INPUT = "bt_d3b_"+i+"_client_points.dat";
			String CLIENT_POINTS_TXT_OUTPUT = cuenta+"_bt_d3b_"+i+"_client_points_output.txt";
			String CLIENT_POINTS_TXT_GOOD = "bt_d3b_"+i+"_client_points_good.dat.txt";
			
			String IMAGE_INPUT = "DE13572468C.jpg";
			String IMAGE_OUTPUT = cuenta+"_idImage_DE13572468C.jpg";
			
			logMessage = cuenta+"; P11; T"+i;
	
			// M07 = ENTREGABLE
			out.println("<hr><h3>"+Msgs.getMsg(Msgs.M07, lang)+" 3  - Test "+i+"/"+numTests+"</h3>");
			
			
			// ofrecemos la consulta de los ficheros de input y los esperados a la salida
			// el primer pii es el prefijo, va al webapps, el segundo es la carpeta pii dentro del webapps
			
			out.println("<ul>");
			// M31 = Fichero inicial del hotel
			out.println("<li><a href='/pii/pii/bt11/sources/"+HOTEL+"' target='_blank'>"+Msgs.getMsg(Msgs.M31, lang)+"</a><br>");
			
			// M32 = Fichero de entradas y salidas del hotel
			out.println("<li><a href='/pii/pii/bt11/sources/"+IOS+"' target='_blank'>"+Msgs.getMsg(Msgs.M32, lang)+"</a><br>");
			
			// M33 = Fichero final del hotel esperado
			out.println("<li><a href='/pii/pii/bt11/sources/"+HOTEL_OUTPUT_GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M33, lang)+"</a><br>");
			
			// M20 = Fichero inicial de clientes
			out.println("<li>"+Msgs.getMsg(Msgs.M20, lang)+"<br>");
			
			// M41 = Fichero TXT final de clientes esperado
			out.println("<li><a href='/pii/pii/bt11/sources/"+CLIENTS_OUTPUT_GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M41, lang)+"</a><br>");
			
			// M38 = Fichero TXT final de puntos esperado
			out.println("<li><a href='/pii/pii/bt11/sources/"+CLIENT_POINTS_TXT_GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M38, lang)+"</a><br>");
			
			// M56 = Fichero final binario de clientes esperado
			out.println("<li><a href='/pii/pii/bt11/sources/"+CLIENT_OBJS_GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M56, lang)+"</a><br>");
			
			out.println("</ul>");
			
			// borramos los resultados previos
			// el working_dir es bt10
			
			builderRm.command("rm", "-f", "outputs/"+HOTEL_OUTPUT);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			builderRm.command("rm", "-f", "outputs/"+CLIENTS_OUTPUT);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			builderRm.command("rm", "-f", "outputs/"+CLIENT_OBJS_OUTPUT);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'

			builderRm.command("rm", "-f", "outputs/"+CLIENT_POINTS_TXT_OUTPUT);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			builderRm.command("rm", "-f", IMAGE_OUTPUT);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			builderRm.command("rm", "-f", result_rm_output_filename, result_rm_error_filename, result_java_output_filename, result_java_error_filename, result_diff_output_filename, result_diff_error_filename);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			// suponemos que todo ha ido bien 
			
			// ejecutamos la práctica
			// el working_dir es bt10 
			
			command = new ArrayList<String>(Arrays.asList(JAVA, "-cp", practicasFolder, P11_PKG_CLASS, "sources/"+HOTEL, "sources/"+IOS, "outputs/"+HOTEL_OUTPUT, "sources/"+CLIENTS, "outputs/"+CLIENTS_OUTPUT, "outputs/"+CLIENT_OBJS_OUTPUT, "sources/"+CLIENT_POINTS_OBJ_INPUT, "outputs/"+CLIENT_POINTS_TXT_OUTPUT));
			builderJava.command(command);
			processJava = builderJava.start(); // ejecutamos la práctica
			timer = processJava.waitFor(2L, java.util.concurrent.TimeUnit.SECONDS);  // esperamos a que termine la ejecución de la práctica
			
			// ponemos un enlace a los prints de la práctica
			
	        if (result_java_output_fd.length() > 0) {
        		// M47 = Fichero con los mensajes que imprime la práctica
	        	out.println("<a href='/pii/pii/bt11/outputs/"+cuenta+"_result_java_output_"+i+"' target='_blank'>"+Msgs.getMsg(Msgs.M47, lang)+"</a><p>");
	        }
	        else
	        	// M48 = la práctica no ha generado ningún mensaje
	        	out.println(Msgs.getMsg(Msgs.M48, lang)+"<p>");
			
			
			
			
			// puede que haya errores en la ejecución de la práctica, aunque pueden ser sólo warnings
			
			if (!timer) {
				processJava.destroy();
                hay_errors = true;
                // M36 el programa no terminó tras 1 segundo
                out.println("<span class='error'>"+Msgs.getMsg(Msgs.M36, lang)+"</span><br>");  // presentamos el mensaje de error en pantalla           
            }
			
	        if (result_java_error_fd.length() > 0) {
	        		// el fichero de errores no está vacío
	        		input = new Scanner(result_java_error_fd);
	                while (input.hasNext()) { // leemos todas las líneas del fichero de errores
	                        linea = input.nextLine();
	                        if (linea.contains("NOTE: Picked up JDK_JAVA_OPTIONS")) continue;  // esta línea es un warning habitual, nos lo saltamos
	                        // es un error real
	                        if (!hay_errors) {  // si el flag de errores aun no está puesto, añadimos un mensaje y lo levantamos
	                        	// M22 = Errores en la ejecución de la práctica
	                            out.println("<b>"+Msgs.getMsg(Msgs.M22, lang)+"</b><br>");
	                            hay_errors = true;  // indicamos que hay hay realmente errores
	                        }
	                        out.println("<span class='error'>"+linea+"</span><br>");  // presentamos el mensaje de error en pantalla
	                }
	
	                input.close();
	        }
	        
	        if (hay_errors)  {
	        	logMessage = logMessage+"; "+ERROR_JAVA;  // ERROR_JAVA = 1, errores al ejecutar la práctica (p.e. NullPointerException)
	        	if (!esProfesor) logPIIChecker(logMessage);
	        	continue; // siguiente test
	        }
	        
        	// no hay errores en la ejecución de la práctica, vamos a ver las diferencias en los resultados
        

	        
	        
	        
	        // Comprobación 1: ver diferencias con el hotel final
			
	        // M43 = Comprobando diferencias en el fichero final del hotel
			out.println("<b>1/5. "+Msgs.getMsg(Msgs.M43, lang)+"</b><br>");
			
        	// ejecutamos el comando 'diff'
        	// el working_dir es bt10
        
        	builderDiff.command("diff", "-w", "outputs/"+HOTEL_OUTPUT, "sources/"+HOTEL_OUTPUT_GOOD);
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		
        		continue;   // si falla una cosa, cortamos el test
        	}

        	
         	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
    		
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		// M26 = Fichero de salida que produce la práctica
        		out.println("<a href='/pii/pii/bt11/outputs/"+HOTEL_OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;  // si falla una cosa, cortamos el test
        	}

        	// M25 = NINGUNA DIFERENCIA: TODO BIEN
        	out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
        	out.println("******<br>");    

        	
        	
        	
        	
        	
        	// Comprobación 2: ver diferencias con el fichero de clientes final
        	
        	// M44 = Comprobando diferencias en el fichero final de clientes
        	out.println("<b>2/5. "+Msgs.getMsg(Msgs.M44, lang)+"</b><br>");
			
        	// ejecutamos el comando 'diff'
        	// el working_dir es bt10
        	
        	builderDiff.command("diff", "-w", "outputs/"+CLIENTS_OUTPUT, "sources/"+CLIENTS_OUTPUT_GOOD);
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue; // siguiente test
        	}

            	
         	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
    		
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		// M26 = Fichero de salida que produce la práctica
        		out.println("<a href='/pii/pii/bt11/outputs/"+CLIENTS_OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;  // si falla una cosa, cortamos el test
        	}
        	
        	// M25 = NINGUNA DIFERENCIA: TODO BIEN
        	out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
        	out.println("******<br>");    

        	
        	
        	
        	
        	
        	
        	
           	// Comprobación 3: ver diferencias con el fichero final binario de clientes
        	
        	// M50 = Comprobando diferencias en el fichero binario de clientes
        	out.println("<b>3/5. "+Msgs.getMsg(Msgs.M50, lang)+"</b><br>");
			
        	// ejecutamos el comando 'diff'
        	// el working_dir es bt10
        	
        	builderDiff.command("diff", "-w", "outputs/"+CLIENT_OBJS_OUTPUT, "sources/"+CLIENT_OBJS_GOOD);
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;
        	}

            	
         	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
    		
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		
        		// M26 = Fichero de salida que produce la práctica
        		out.println("<a href='/pii/pii/bt11/outputs/"+CLIENT_OBJS_OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;  // si falla una cosa, cortamos el test
        	}

        	
    		// M25 = NINGUNA DIFERENCIA: TODO BIEN
    		out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
        	out.println("******<br>");    
        	
        	
        	
        	
        	
 
           	// Comprobación 4: ver diferencias con el fichero final TXT de puntos
        	
        	// M52 = Comprobando diferencias en el fichero final de texto de puntos
        	out.println("<b>4/5. "+Msgs.getMsg(Msgs.M52, lang)+"</b><br>");
			
        	// ejecutamos el comando 'diff'
        	// el working_dir es bt10
        	
        	builderDiff.command("diff", "-w", "outputs/"+CLIENT_POINTS_TXT_OUTPUT, "sources/"+CLIENT_POINTS_TXT_GOOD); 
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;
        	}

            	
         	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
    		
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		
        		// M26 = Fichero de salida que produce la práctica
        		out.println("<a href='/pii/pii/bt11/outputs/"+CLIENT_POINTS_TXT_OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;  // si falla una cosa, cortamos el test
        	}

        	
    		// M25 = NINGUNA DIFERENCIA: TODO BIEN
    		out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
        	out.println("******<br>");    
        	
        	
        	
        	
        	
        	
        	
           	// Comprobación 5: ver diferencias con la primera imagen
        	
        	// M53 = Comprobando diferencias en el fichero de una imagen
        	out.println("<b>5/5. "+Msgs.getMsg(Msgs.M53, lang)+"</b><br>");
			
        	// ejecutamos el comando 'diff'
        	// el working_dir es bt10
        	
        	builderDiff.command("diff", "-w", IMAGE_INPUT, IMAGE_OUTPUT);   // sources --> outputs
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;
        	}

            	
         	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
    		
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;  // si falla una cosa, cortamos el test
        	}

        	
    		// M25 = NINGUNA DIFERENCIA: TODO BIEN
    		out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
        	out.println("******<br>");    
        	
        	
    		resultado--;  // este test fue bien
    		logMessage = logMessage+"; "+TEST_OK;  // TEST_OK = 0, todo OK
        	
        	
        	if (!esProfesor) logPIIChecker(logMessage);
        	
		}  // fin de los tests
		
		return resultado;  // debería ser 0 si todos los tests fueron bien
        
	}
	
	
		
		
	
	
	
	
	
	

	
	
	// ordena corregir P12
	// devuelve 0 si pasa todos los tests
	
	public int doGetCorrectP12 (PrintWriter out, String cuenta, String lang, boolean esProfesor)
			throws Exception, IOException
	{
		final String P12_PKG_CLASS = "e3.p12.P12";
		final String P12_CLASS_FILE = "P12.class";
		File fd, fdMap;
		
		int resultado;
		List<String> command;
		Scanner input;
		String linea, logMessage = "";
		boolean hay_errors=false, hay_diffs=false;
		
		// M40 = Corrigiendo la P12 de 
		out.println("<h3>"+String.format(Msgs.getMsg(Msgs.M40, lang), "P12")+cuenta+"</h3>");
		
		// M13 = Practicas
		String practicasFolder = PII+"pii"+cuenta+"/"+Msgs.getMsg(Msgs.M13, lang)+"/";
		String p12Folder = practicasFolder+"e3/p12/";  // localización de la práctica del alumno
        
		fd = new File (p12Folder);
        if (!fd.canRead()) {
        	// M14 = "El directorio 'Practicas/e3/p12' de la cuenta no existe o no tiene permisos 755 para entrar"));
            throw (new Exception(String.format(Msgs.getMsg(Msgs.M14, lang), Msgs.getMsg(Msgs.M13, lang)+"/e3/p12", cuenta)));
        }
        
        String classFile = p12Folder+"/"+P12_CLASS_FILE; 	// práctica del alumno
        fd = new File (classFile);
        if (!fd.exists()) {
        	// M18 = En el directorio 'Practicas/e3/p12' no está el fichero 
            throw (new Exception(String.format(Msgs.getMsg(Msgs.M18, lang), Msgs.getMsg(Msgs.M13, lang)+"/e3/p12")+P12_CLASS_FILE));
        }
        
        
        // directorio de la cuenta de SINT donde están los ficheros de entrada y salida de la batería
        // directorio donde voy a guardar los resultados de la ejecución de los comandos (error y output)
        String wd_fileName = SINT0_PII_RT+"bt12/"; 
		String sources_fileName = wd_fileName+"sources/";
		String outputs_fileName = wd_fileName+"outputs/";  

		
		File wd_fd = new File(wd_fileName);
		if (!wd_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+wd_fileName+"'"));
        }
		
		File sources_fd = new File(sources_fileName);
		if (!sources_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+sources_fileName+"'"));
        }
		
		File outputs_fd = new File(outputs_fileName);
		if (!outputs_fd.canRead()) {
			// M19 = No existe (o no se puede entrar en) el directorio
            throw (new Exception(Msgs.getMsg(Msgs.M19, lang)+" '"+outputs_fileName+"'"));
        }
		
		String result_rm_output_filename = outputs_fileName+cuenta+"_result_rm_output";	// destino de los mensajes de output de rm
		String result_rm_error_filename = outputs_fileName+cuenta+"_result_rm_error";	// destino de los mensajes de error de rm
		File result_rm_output_fd = new File(result_rm_output_filename);
		File result_rm_error_fd = new File(result_rm_error_filename);
		
		String result_java_output_filename_base = outputs_fileName+cuenta+"_result_java_output_";	// destino de los mensajes de output de java
		String result_java_error_filename = outputs_fileName+cuenta+"_result_java_error";	// destino de los mensajes de error de java
		// lo creo uno a uno en cada test
		// File result_java_output_fd = new File(result_java_output_filename);
		File result_java_error_fd = new File(result_java_error_filename);
		
		String result_diff_output_filename = outputs_fileName+cuenta+"_result_diff_output";	// destino de los mensajes de output de diff
		String result_diff_error_filename = outputs_fileName+cuenta+"_result_diff_error";	// destino de los mensajes de error de diff
		File result_diff_output_fd = new File(result_diff_output_filename);
		File result_diff_error_fd = new File(result_diff_error_filename);

		
		// vamos a ejecutar los procesos de la corrección
		
		Process processRm;
		ProcessBuilder builderRm = new ProcessBuilder();
		builderRm.directory(wd_fd);					    // directorio de trabajo de los comandos rm  (bt10)
		builderRm.redirectOutput(result_rm_output_fd);	// output de los comandos rm
		builderRm.redirectError(result_rm_error_fd);	// error de los comandos rm
		
		Process processJava;
		ProcessBuilder builderJava = new ProcessBuilder();
		builderJava.directory(wd_fd);						// directorio de trabajo de los comandos java (bt10)
		// el output debe ser distinto para cada test, ya que se ofrece en el interfaz
		// builderJava.redirectOutput(result_java_output_fd);	// output de los comandos java
		builderJava.redirectError(result_java_error_fd);	// error de los comandos java
		
		Process processDiff;
		ProcessBuilder builderDiff = new ProcessBuilder();
		builderDiff.directory(wd_fd);						// directorio de trabajo de los comandos diff (bt10)
		builderDiff.redirectOutput(result_diff_output_fd);	// output de los comandos diff
		builderDiff.redirectError(result_diff_error_fd);	// error de los comandos diff
		
		
		int numTests=2;	
		resultado = numTests;
		
		// Comienza la corrección
		
		String i;
		boolean timer;
		for (int x=1; x<=numTests; x++) {
			hay_errors= false;
			hay_diffs=false;
			timer=false;

			i = String.format("%02d", x);
			
			String result_java_output_filename = result_java_output_filename_base+i;
			File result_java_output_fd = new File(result_java_output_filename);
			builderJava.redirectOutput(result_java_output_fd);	// output de los comandos java
			
			String HOTEL = "bt_d3c_"+i+"_hotel.txt";
			String IOS = "bt_d3c_"+i+"_ios.txt";
			
			String HOTEL_OUTPUT = cuenta+"_bt_d3c_"+i+"_hotel_output.txt";
			String HOTEL_OUTPUT_GOOD = "bt_d3c_"+i+"_hotel_output_good.txt";
			
			String CLIENTS = "bt_d3c_"+i+"_clients.txt";
			String CLIENTS_OUTPUT = cuenta+"_bt_d3c_"+i+"_clients_output.txt";
			String CLIENTS_OUTPUT_GOOD = "bt_d3c_"+i+"_clients_output_good.txt";
			
			String PAYMENTS_TXT_OUTPUT = cuenta+"_bt_d3c_"+i+"_payments_output.txt";
			String PAYMENTS_TXT_GOOD = "bt_d3c_"+i+"_payments_good.txt";
			

			
			logMessage = cuenta+"; P12; T"+i;
	
			// M07 = ENTREGABLE
			out.println("<hr><h3>"+Msgs.getMsg(Msgs.M07, lang)+" 3  - Test "+i+"/"+numTests+"</h3>");
			
			
			// ofrecemos la consulta de los ficheros de input y los esperados a la salida
			// el primer pii es el prefijo, va al webapps, el segundo es la carpeta pii dentro del webapps
			
			out.println("<ul>");
			// M31 = Fichero inicial del hotel
			out.println("<li><a href='/pii/pii/bt12/sources/"+HOTEL+"' target='_blank'>"+Msgs.getMsg(Msgs.M31, lang)+"</a><br>");
			
			// M32 = Fichero de entradas y salidas del hotel
			out.println("<li><a href='/pii/pii/bt12/sources/"+IOS+"' target='_blank'>"+Msgs.getMsg(Msgs.M32, lang)+"</a><br>");
			
			// M33 = Fichero final del hotel esperado
			out.println("<li><a href='/pii/pii/bt12/sources/"+HOTEL_OUTPUT_GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M33, lang)+"</a><br>");
			
			// M20 = Fichero inicial de clientes
			out.println("<li><a href='/pii/pii/bt12/sources/"+CLIENTS+"' target='_blank'>"+Msgs.getMsg(Msgs.M20, lang)+"</a><br>");
			
			// M41 = Fichero TXT final de clientes esperado
			out.println("<li><a href='/pii/pii/bt12/sources/"+CLIENTS_OUTPUT_GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M41, lang)+"</a><br>");
			
			// M39 = Fichero TXT final de pagos esperado
			out.println("<li><a href='/pii/pii/bt12/sources/"+PAYMENTS_TXT_GOOD+"' target='_blank'>"+Msgs.getMsg(Msgs.M39, lang)+"</a><br>");
			
			out.println("</ul>");
			
			// borramos los resultados previos
			// el working_dir es bt10
			
			builderRm.command("rm", "-f", "outputs/"+HOTEL_OUTPUT);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			builderRm.command("rm", "-f", "outputs/"+CLIENTS_OUTPUT);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			builderRm.command("rm", "-f", "outputs/"+PAYMENTS_TXT_OUTPUT);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			builderRm.command("rm", "-f", result_rm_output_filename, result_rm_error_filename, result_java_output_filename, result_java_error_filename, result_diff_output_filename, result_diff_error_filename);
			processRm = builderRm.start();  // ejecutamos el comando 'rm'
			processRm.waitFor();  // esperamos a que termine el comando 'rm'
			
			// suponemos que todo ha ido bien 
			
			// ejecutamos la práctica
			// el working_dir es bt10 
			
			command = new ArrayList<String>(Arrays.asList(JAVA, "-cp", practicasFolder, P12_PKG_CLASS, "sources/"+HOTEL, "sources/"+IOS, "outputs/"+HOTEL_OUTPUT, "sources/"+CLIENTS, "outputs/"+CLIENTS_OUTPUT, "outputs/"+PAYMENTS_TXT_OUTPUT));
			builderJava.command(command);
			processJava = builderJava.start(); // ejecutamos la práctica
			timer = processJava.waitFor(2L, java.util.concurrent.TimeUnit.SECONDS);  // esperamos a que termine la ejecución de la práctica
			
			// ponemos un enlace a los prints de la práctica
			
	        if (result_java_output_fd.length() > 0) {
        		// el fichero de output no está vacío
	        	out.println("<a href='/pii/pii/bt12/outputs/"+cuenta+"_result_java_output_"+i+"' target='_blank'>"+Msgs.getMsg(Msgs.M47, lang)+"</a><p>");
	        }
	        else
	        	// M48 = la práctica no ha generado ningún mensaje
	        	out.println(Msgs.getMsg(Msgs.M48, lang)+"<p>");
			
			
			
			
			// puede que haya errores en la ejecución de la práctica, aunque pueden ser sólo warnings
			
			if (!timer) {
				processJava.destroy();
                hay_errors = true;
                // M36 el programa no terminó tras 1 segundo
                out.println("<span class='error'>"+Msgs.getMsg(Msgs.M36, lang)+"</span><br>");  // presentamos el mensaje de error en pantalla           
            }
			
	        if (result_java_error_fd.length() > 0) {
	        		// el fichero de errores no está vacío
	        		input = new Scanner(result_java_error_fd);
	                while (input.hasNext()) { // leemos todas las líneas del fichero de errores
	                        linea = input.nextLine();
	                        if (linea.contains("NOTE: Picked up JDK_JAVA_OPTIONS")) continue;  // esta línea es un warning habitual, nos lo saltamos
	                        // es un error real
	                        if (!hay_errors) {  // si el flag de errores aun no está puesto, añadimos un mensaje y lo levantamos
	                        	// M22 = Errores en la ejecución de la práctica
	                            out.println("<b>"+Msgs.getMsg(Msgs.M22, lang)+"</b><br>");
	                            hay_errors = true;  // indicamos que hay hay realmente errores
	                        }
	                        out.println("<span class='error'>"+linea+"</span><br>");  // presentamos el mensaje de error en pantalla
	                }
	
	                input.close();
	        }
	        
	        if (hay_errors)  {
	        	logMessage = logMessage+"; "+ERROR_JAVA;  // ERROR_JAVA = 1, errores al ejecutar la práctica (p.e. NullPointerException)
	        	if (!esProfesor) logPIIChecker(logMessage);
	        	continue; // siguiente test
	        }
	        
        	// no hay errores en la ejecución de la práctica, vamos a ver las diferencias en los resultados
        

	        
	        
	       	
        	// Comprobación 1: ver diferencias con el fichero de clientes final
        	
        	// M44 = Comprobando diferencias en el fichero final de clientes
        	out.println("<b>1/3. "+Msgs.getMsg(Msgs.M44, lang)+"</b><br>");
			
        	// ejecutamos el comando 'diff'
        	// el working_dir es bt12
        	
        	builderDiff.command("diff", "-w", "outputs/"+CLIENTS_OUTPUT, "sources/"+CLIENTS_OUTPUT_GOOD);
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue; // siguiente test
        	}

            	
         	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
    		
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		// M26 = Fichero de salida que produce la práctica
        		out.println("<a href='/pii/pii/bt12/outputs/"+CLIENTS_OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;  // si falla una cosa, cortamos el test
        	}
        	
        	// M25 = NINGUNA DIFERENCIA: TODO BIEN
        	out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
        	out.println("******<br>");    

        	
        	
        	
        	
	        
	        
			File fdh = new File(wd_fileName+"sources/"+HOTEL_OUTPUT_GOOD);
			
			if (!fdh.exists()) {
				// M37 = El resto de tests no proceden en este escenario
				out.println("<br><b>"+Msgs.getMsg(Msgs.M37, lang)+"</b><br>");
				
				resultado--;  // este test fue bien
	    		logMessage = logMessage+"; "+TEST_OK;  // TEST_OK = 0, todo OK
	    		if (!esProfesor) logPIIChecker(logMessage);
	    		
				continue;
			}
	
			
	        // Comprobación 2: ver diferencias con el hotel final
			
	        // M43 = Comprobando diferencias en el fichero final del hotel
			out.println("<b>2/3. "+Msgs.getMsg(Msgs.M43, lang)+"</b><br>");
			

        	// ejecutamos el comando 'diff'
        	// el working_dir es bt12
        
        	builderDiff.command("diff", "-w", "outputs/"+HOTEL_OUTPUT, "sources/"+HOTEL_OUTPUT_GOOD);
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		
        		continue;   // si falla una cosa, cortamos el test
        	}

        	
         	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
    		
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		// M26 = Fichero de salida que produce la práctica
        		out.println("<a href='/pii/pii/bt12/outputs/"+HOTEL_OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;  // si falla una cosa, cortamos el test
        	}

        	// M25 = NINGUNA DIFERENCIA: TODO BIEN
        	out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
        	out.println("******<br>");    

     
        	
        	
        	
        	
        	
        	
        	
			File fdp = new File(wd_fileName+"sources/"+PAYMENTS_TXT_GOOD);
			
			if (!fdp.exists()) {
				// M37 = El resto de tests no proceden en este escenario
				out.println("<br><b>"+Msgs.getMsg(Msgs.M37, lang)+"</b><br>");
				
				resultado--;  // este test fue bien
	    		logMessage = logMessage+"; "+TEST_OK;  // TEST_OK = 0, todo OK
	    		if (!esProfesor) logPIIChecker(logMessage);
	    		
				continue;
			}
 
           	// Comprobación 3: ver diferencias con el fichero final TXT de pagos
        	
        	// M55 = Comprobando diferencias en el fichero final de texto de pagos
        	out.println("<b>3/3. "+Msgs.getMsg(Msgs.M55, lang)+"</b><br>");
			
			
        	// ejecutamos el comando 'diff'
        	// el working_dir es bt10
        	
        	builderDiff.command("diff", "-w", "outputs/"+PAYMENTS_TXT_OUTPUT, "sources/"+PAYMENTS_TXT_GOOD); 
        	processDiff = builderDiff.start();
        	processDiff.waitFor(); // esperamos a la finalización del 'diff'
		
        	if (result_diff_error_fd.length() > 0) { 
        		logMessage = logMessage+"; "+ERROR_DIFF;   // ERROR_DIFF = 2, errores al ejecutar el diff  (p.e. no hay fichero de salida)
        		
        		// hubo errores en la ejecución del diff
        		// M23 = Errores en la ejecución del diff
        		out.println("<br><b>"+Msgs.getMsg(Msgs.M23, lang)+"</b><br>");
        		input = new Scanner(result_diff_error_fd);

        		while (input.hasNext()) {  
        			linea = input.nextLine();
        			out.println("<span class='error'>"+linea+"</span><br>");
        		}
			
        		input.close();
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;
        	}

            	
         	// no hubo errores al ejecutar el diff, vamos a ver las diferencias en el resultado
    		
        	out.println("******<br>");

        	input = new Scanner(result_diff_output_fd);

        	while (input.hasNext()) {
        		hay_diffs = true;  // si entra es que el fichero de diferencias no está vacío, pone a true el flag hay_diffs
        		linea = input.nextLine();
        		out.println("<span class='error'>"+linea+"</span><br>");
        	}

        	input.close();
        	
        	if (hay_diffs) {  // hay diffs
        		logMessage = logMessage+"; "+DIFFS;   // DIFFS = 3, diferencias en los resultados
        		
        		// M26 = Fichero de salida que produce la práctica
        		out.println("<a href='/pii/pii/bt12/outputs/"+PAYMENTS_TXT_OUTPUT+"' target='_blank'>"+Msgs.getMsg(Msgs.M26, lang)+"</a><p>");
        		
        		if (!esProfesor) logPIIChecker(logMessage);
        		continue;  // si falla una cosa, cortamos el test
        	}

        	
    		// M25 = NINGUNA DIFERENCIA: TODO BIEN
    		out.println("<span class='bien'>"+Msgs.getMsg(Msgs.M25, lang)+"</span><br>");
        	out.println("******<br>");    
        	
        	
    		resultado--;  // este test fue bien
    		logMessage = logMessage+"; "+TEST_OK;  // TEST_OK = 0, todo OK
        	
        	
        	if (!esProfesor) logPIIChecker(logMessage);
        	
		}  // fin de los tests
		
		return resultado;  // debería ser 0 si todos los tests fueron bien
        
	}
		
	
	
	
	
		
		
		
	/*
		
	public boolean readAndCompareBinaryCars (String filename1, String filename2) {
		ObjectInputStream ois1, ois2;
		ArrayList<Car> a1 = new ArrayList<Car>(), a2 = new ArrayList<Car>();

		try {
			ois1 = new ObjectInputStream(new FileInputStream(filename1));
			ois2 = new ObjectInputStream(new FileInputStream(filename2));

			try {
				while (true) 
					a1.add((Car)ois1.readObject());
			}
			catch (EOFException e) {ois1.close();}

			try {
				while (true) 
					a2.add((Car)ois2.readObject());
			}
			catch (EOFException e) {ois2.close();}
		}
		catch (IOException e) {
			System.out.println(e.toString());
			return false;
		}
		catch (ClassNotFoundException e) {
			System.out.println("Problema ClassNotFoundException leyendo un Car: "+e.toString());
			return false;
		}

		if (a1.size() != a2.size()) return false;

		for (int i=0; i < a1.size(); i++) {
			if (a1.get(i).compareTo(a2.get(i)) != 0) return false;
		}

		return true;
	} 
		
		*/
	
	public void printHeader (PrintWriter out, String lang) {
		// M00 = Servicio de corrección
		out.println("<html><head><meta charset='utf-8'/><title>"+Msgs.getMsg(Msgs.M00, lang)+"</title>");
        out.println("<link rel='stylesheet'  type='text/css' href='/pii/pii/pii.css'/></head>");
        out.println("<body>");
        // M01 = Programación II
        out.println("<div id='asignatura'>"+Msgs.getMsg(Msgs.M01, lang)+"</div>");
        // M02 = EE Teleco , Universidad de Vigo
        out.println("<div id='grado'>"+Msgs.getMsg(Msgs.M02, lang)+"</div>");
        // M03 = Servicio de verificación de prácticas del curso 
        out.println("<div id='servicio' >"+Msgs.getMsg(Msgs.M03, lang)+"</div>");
	}	
	
	public void printBBC (PrintWriter out, String lang, String cuenta, String dni) {
		out.println("<form action='?' method='GET'>");
		out.println("<input type='hidden' name='cuenta' value='"+cuenta+"'>");
		out.println("<input type='hidden' name='dni' value='"+dni+"'>");

		// M04 = Inicio
        out.println("<input class='home' type='submit' value='"+Msgs.getMsg(Msgs.M04, lang)+"'>");
        out.println("</form>");
        
        printCopyright(out); 
	}	
	
	public void printCopyright (PrintWriter out) {
		out.println("<hr>");
		out.println("<center>&copy; Alberto Gil Solla</center>");
		out.println("</body></html>");
	}
	
	public void sendError (PrintWriter out, String errorMessage, String lang)  throws IOException
	{
		out.println("<span class='error'>Error: "+errorMessage+"</span><br>");
		this.printBBC(out, lang, "", "");
	
	}


}
