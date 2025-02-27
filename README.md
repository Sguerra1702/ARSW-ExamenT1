
### Escuela Colombiana de Ingeniería
### Arquitecturas de Software - ARSW
## Parcial 1 - Paralelismo - Hilos - Caso BlackListSearch
## Desarrollado por Santiago Guerra Penagos

### Descripción
  Este es un ejercicio de programación con hilos en Java, que permite su aplicación a un caso concreto.
  
** Ejercicio Black List Search**


Para un software de vigilancia automática de seguridad informática se está desarrollando un componente encargado de validar las direcciones IP en varios miles de listas negras (de host maliciosos) conocidas, y reportar aquellas que existan en al menos cinco de dichas listas. 

Dicho componente está diseñado de acuerdo con el siguiente diagrama, donde:

- HostBlackListsDataSourceFacade es una clase que ofrece una 'fachada' para realizar consultas en cualquiera de las N listas negras registradas (método 'isInBlacklistServer'), y que permite también hacer un reporte a una base de datos local de cuando una dirección IP se considera peligrosa. Esta clase NO ES MODIFICABLE, pero se sabe que es 'Thread-Safe'.

- HostBlackListsValidator es una clase que ofrece el método 'checkHost', el cual, a través de la clase 'HostBlackListDataSourceFacade', valida en cada una de las listas negras un host determinado. En dicho método está considerada la política de que al encontrarse un HOST en al menos cinco listas negras, el mismo será registrado como 'no confiable', o como 'confiable' en caso contrario. Adicionalmente, retornará la lista de los números de las 'listas negras' en donde se encontró registrado el HOST.

![](img/Model.png)

Al usarse el módulo, la evidencia de que se hizo el registro como 'confiable' o 'no confiable' se dá por lo mensajes de LOGs:

INFO: HOST 205.24.34.55 Reported as trustworthy

INFO: HOST 205.24.34.55 Reported as NOT trustworthy


Al programa de prueba provisto (Main), le toma sólo algunos segundos análizar y reportar la dirección provista (200.24.34.55), ya que la misma está registrada más de cinco veces en los primeros servidores, por lo que no requiere recorrerlos todos. Sin embargo, hacer la búsqueda en casos donde NO hay reportes, o donde los mismos están dispersos en las miles de listas negras, toma bastante tiempo.

Éste, como cualquier método de búsqueda, puede verse como un problema vergonzosamente paralelo, ya que no existen dependencias entre una partición del problema y otra.

Para 'refactorizar' este código, y hacer que explote la capacidad multi-núcleo de la CPU del equipo, realice lo siguiente:

1. Cree una clase de tipo Thread que represente el ciclo de vida de un hilo que haga la búsqueda de un segmento del conjunto de servidores disponibles. </br>
	### Desarrollo
	Se creó la clase BlackListValidatorThread: 
	![](/img/1.png)

2. Agregue al método 'checkHost' un parámetro entero N, correspondiente al número de hilos entre los que se va a realizar la búsqueda (recuerde tener en cuenta si N es par o impar!). Modifique el código de este método para que divida el espacio de búsqueda entre las N partes indicadas, y paralelice la búsqueda a través de N hilos. </br>
	### Desarrollo
	En el método checkHost se crearon una variable llamada ```segmentSize ```la cual define el tamaño de cada segmento a analizar en la lista de servidores, además, se definió un arreglo de Hilos tipo BlackListValidatorThread donde cada posicion tiene un hilo que busca en un segmento asignado:
	![](/img/2.png)


3. Haga que entre TODOS los hilos lleven la cuenta de las ocurrencias de la IP que se han detectado en TOTAL y una vez se cumpla el número objetivo (_BLACK_LIST_ALARM_COUNT_), deben finalizar el procesamiento TODOS los hilos, luego debe reportar el host como confiable o no confiable. Tenga también en cuenta:  

	* No se deben generar esperas activas nuevas, ni condiciones carrera. 
	
	* Se sabe que el HOST 202.24.34.55 está reportado en listas negras de una forma más dispersa, y que el host 212.24.24.55 NO está en ninguna lista negra.
	### Desarrollo
	Cada hilo lleva la cuenta de cuantas ocurrencias hay del host (si es que las hay) dentro de las listas negras que se están analizando, esto se puede ver en el método run de la clase BlackListValidatorThread:
	![](/img/3.png)
	Al ejecutar la clase ```Main.java``` usando los 3 hosts que se mencionan en el enunciado vemos los siguientes resultados: </br>
	a. Para el host 200.24.34.55 (Está registrado más de 5 veces en los primeros servidores):
	![](/img/4.png)

	b. Para el host 202.24.34.55 (Reportado de manera más dispersa):
	![](/img/5.png)

	c. Para el host 212.24.24.55 (No se encuentra en los registros, es un host seguro):
	![](/img/6.png)



**Bono**

Haga que también imprima el número TOTAL de registros revisados en las listas de todos los hilos, es decir, imprimir el número de listas negras revisadas VS. el número de listas negras total(80,000). Se debe garantizar que dicha información sea verídica bajo el nuevo esquema de procesamiento en paralelo planteado.