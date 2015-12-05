package home.master.geogame;



import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class MapActivity extends FragmentActivity {
	
	//Constantes
	private final int TODOS		= -1; 
	private final int EUROPA	= 0;
	private final int ASIA		= 1;
	private final int AMERICA	= 2;
	private final int AFRICA 	= 3;
	private final int OCEANIA 	= 4; 
	
	//Atributos de clase
	private GoogleMap mapa;
	private HashSet<Integer> continentes;
	private SparseArray<ArrayList<Marker>> marcadores;
	private SparseArray<ArrayList<Marker>> marcadoresVisitados;
	private boolean gameOver;
	private int aciertos;
	private int fallos;
	private int continenteSeleccionado;	
	private TextView textPregunta;
	private TextView contadorAciertos;
	private TextView contadorFallos;
	private Marker marcadorActual;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		mapa = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

		textPregunta = (TextView)findViewById(R.id.textQuestion);
		contadorAciertos = (TextView)findViewById(R.id.textAciertosCount);
		contadorFallos = (TextView)findViewById(R.id.textFallosCount);
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		inicializa();
		generarPregunta();
		
		mapa.setOnMarkerClickListener(new OnMarkerClickListener() {			
			public boolean onMarkerClick(Marker marker) {
				if(!gameOver){
					checkAnswer(marker);
					marker.showInfoWindow();
					generarPregunta();
				}
				return true;
			}
		});
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {	
		getMenuInflater().inflate(R.menu.menu_map, menu);
		return super.onCreateOptionsMenu(menu);
	}

	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if(!item.isChecked()){
			item.setChecked(true);
		}	
		
		switch(item.getItemId()){
		
		case R.id.item_africa:
			continenteSeleccionado = AFRICA;
			break;
			
		case R.id.item_america:
			continenteSeleccionado = AMERICA;
			break;
			
		case R.id.item_asia:
			continenteSeleccionado = ASIA;
			break;
			
		case R.id.item_europa:
			continenteSeleccionado = EUROPA;
			break;
			
		case R.id.item_oceania:
			continenteSeleccionado = OCEANIA;
			break;
			
		case R.id.item_todos:
			continenteSeleccionado = TODOS;
			break;
		}
		
		reinicia(continenteSeleccionado);
		muestraYOculta(continenteSeleccionado);
		generarPregunta();
		
		return super.onOptionsItemSelected(item);
	}



	
	/**
	 * muestra los marcadores del continente pasado por parámetro y oculta todos los demás
	 * si el continente es TODOS, muestra todos los marcadores
	 * @param continente
	 */
	private void muestraYOculta(int continente) {
		ArrayList<Marker> array;
		
		for(int i=0;i!=5;i++){
			array = marcadores.get(i);
			for(int j=0;j!=array.size();j++){
				if(continente == i || continente == TODOS){
					array.get(j).setVisible(true);
				}
				else{
					array.get(j).setVisible(false);
				}
			}
			marcadores.setValueAt(i, array);
		}
	}

	
	/**
	 * reinicia la partida
	 * @param continente, continente con el que se va a jugar
	 */
	private void reinicia(int continente) {
		gameOver = false;
		aciertos = 0;
		fallos = 0;
		contadorAciertos.setText(String.valueOf(aciertos));
		contadorFallos.setText(String.valueOf(fallos));
		reiniciaMarcadores();
		continentes.clear();
		if(continente != TODOS){
			continentes.add(continente);
		}
		else{
			for(int i=0;i!=5;i++){
				continentes.add(i);
			}
		}	
	}
	
	
	/**
	 * reinicia los arrays de marcadores de cada continente, de manera que se pueda volver a preguntar por cualquier pais
	 */
	private void reiniciaMarcadores() {
		ArrayList<Marker> visitados;
		ArrayList<Marker> noVisitados;
		Boolean modificado;
		Marker m;
		
		for(int i=0;i!=5;i++){
			visitados = marcadoresVisitados.get(i);
			noVisitados = marcadores.get(i);
			modificado = false;
			
			for(int j=0;j<visitados.size();j++){
				m = visitados.remove(j);
				m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
				noVisitados.add(m);
				modificado = true;
			}
			
			if(modificado){
				marcadoresVisitados.setValueAt(i, visitados);
				marcadores.setValueAt(i, noVisitados);
			}		
		}
	}
	

	/**
	 * inicializa las distintas variables de clase y el mapa, creando los marcadores.
	 */
	private void inicializa() {
		gameOver = false;
		aciertos = 0;
		fallos = 0;
		continenteSeleccionado = TODOS;
		continentes = new HashSet<Integer>();
		continentes.add(EUROPA);
		continentes.add(ASIA);
		continentes.add(AMERICA);
		continentes.add(AFRICA);
		continentes.add(OCEANIA);
		crearMarcadores();
		mapa.setMapType(GoogleMap.MAP_TYPE_SATELLITE);	
	}
	

	/**
	 * método para generar aleatoriamente un pais y mostrar la pregunta por pantalla
	 */
	private int generarPregunta() {
		Random r = new Random();
		int indexContinente,indexPais,paisesRestantes;
		
		//"continenteSeleccionado" representa el continente con el que se juega
		//si vale TODOS se puede preguntar sobre cualquier continente
		if(continenteSeleccionado == TODOS){ 
			indexContinente = r.nextInt(continentes.size());
		}
		else{
			indexContinente = continenteSeleccionado;
		}
		//número de paises que quedan por preguntar del continente "indexContinente"
		paisesRestantes = marcadores.get(indexContinente).size();
		
		//si ya se ha preguntado por todos los paises de todos los continentes se acaba el juego
		if(paisesRestantes == 0){
			continentes.remove(indexContinente);
			if((indexContinente == TODOS && continentes.size() == 0) || (indexContinente != TODOS && !continentes.contains(indexContinente))){
				gameOver();
				return 0;
			}
			indexContinente = r.nextInt(continentes.size());
			paisesRestantes = marcadores.get(indexContinente).size();
		}
		indexPais = r.nextInt(paisesRestantes);
		
		//"marcadorActual" guarda el marker por el que se está preguntando
		marcadorActual = marcadores.get(indexContinente).get(indexPais);
		actualizaVisitados(indexContinente,indexPais);
		
		textPregunta.setText("¿Dónde está " + marcadorActual.getTitle() + " ?");
		
		return 0;
	}
	

	/**
	 * método para que no se vuelva a preguntar por el mismo pais dos veces, poniéndolo en marcadoresVisitados.
	 * @param indexContinente
	 * @param indexPais
	 */
	private void actualizaVisitados(int indexContinente, int indexPais){
		marcadoresVisitados.get(indexContinente).add(marcadorActual);
		marcadores.get(indexContinente).remove(indexPais);
	}

	
	/**
	 * método para comprobar la respuesta y actualizar la interfaz
	 * @param marker representa el marcador en el que se ha pulsado
	 */
	private void checkAnswer(Marker marker){
		if(marker.getTitle().equals(marcadorActual.getTitle())){
			marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
			aciertos++;
			contadorAciertos.setText(String.valueOf(aciertos));
			Toast.makeText(this, "Correcto", Toast.LENGTH_SHORT).show();
		}
		else{
			marcadorActual.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			fallos++;
			contadorFallos.setText(String.valueOf(fallos));
			Toast.makeText(this, "Incorrecto", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	/**
	 * 
	 */
	private void gameOver(){
		gameOver = true;
		textPregunta.setText("Fin de la partida");
	}
	
	
	/**
	 * Se crea un ArrayList por cada continente.
	 *Se crean los markers de los paises, asignándolos al mapa y al ArrayList correspondiente
	 */
	private void crearMarcadores() {
		
		ArrayList<Marker> europa = new ArrayList<Marker>(); 
		europa.add(mapa.addMarker(new MarkerOptions().title("España").position(new LatLng(40.440676, -3.678131)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		europa.add(mapa.addMarker(new MarkerOptions().title("Francia").position(new LatLng(47.189712, 2.453612)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		europa.add(mapa.addMarker(new MarkerOptions().title("Italia").position(new LatLng(42.455888, 12.824706)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		europa.add(mapa.addMarker(new MarkerOptions().title("Irlanda").position(new LatLng(53.225768, -7.7417)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		europa.add(mapa.addMarker(new MarkerOptions().title("Polonia").position(new LatLng(52.536273, 19.284667)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		europa.add(mapa.addMarker(new MarkerOptions().title("Portugal").position(new LatLng(39.164141, -8.708497)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		europa.add(mapa.addMarker(new MarkerOptions().title("Rumanía").position(new LatLng(45.79817, 24.733886)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		europa.add(mapa.addMarker(new MarkerOptions().title("Grecia").position(new LatLng(39.470125, 21.833495)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		europa.add(mapa.addMarker(new MarkerOptions().title("Noruega").position(new LatLng(60.239811, 8.693847)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		europa.add(mapa.addMarker(new MarkerOptions().title("Suiza").position(new LatLng(46.732331,7.919311)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		
		ArrayList<Marker> asia = new ArrayList<Marker>();
		asia.add(mapa.addMarker(new MarkerOptions().title("Japón").position(new LatLng(36.137875, 138.288573)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		asia.add(mapa.addMarker(new MarkerOptions().title("Corea del Sur").position(new LatLng(36.985003, 128.225097)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		asia.add(mapa.addMarker(new MarkerOptions().title("China").position(new LatLng(33.651208, 104.487303)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		asia.add(mapa.addMarker(new MarkerOptions().title("India").position(new LatLng(22.917923, 78.911131)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		asia.add(mapa.addMarker(new MarkerOptions().title("Thailandia").position(new LatLng(15.68651, 101.092529)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		asia.add(mapa.addMarker(new MarkerOptions().title("Turquía").position(new LatLng(39.13006, 35.463867)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		asia.add(mapa.addMarker(new MarkerOptions().title("Arabia Saudí").position(new LatLng(23.563987, 45.037079)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		asia.add(mapa.addMarker(new MarkerOptions().title("Mongolia").position(new LatLng(46.619261, 103.619614)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		asia.add(mapa.addMarker(new MarkerOptions().title("Israel").position(new LatLng(31.161109, 34.820309)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		asia.add(mapa.addMarker(new MarkerOptions().title("Vietnam").position(new LatLng(13.282719, 108.450165)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));

		ArrayList<Marker> america = new ArrayList<Marker>();
		america.add(mapa.addMarker(new MarkerOptions().title("Estados Unidos").position(new LatLng(37.038103, -100.144335)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		america.add(mapa.addMarker(new MarkerOptions().title("Cuba").position(new LatLng(21.872842, -78.937062)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		america.add(mapa.addMarker(new MarkerOptions().title("México").position(new LatLng(23.926013, -102.44339)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		america.add(mapa.addMarker(new MarkerOptions().title("Perú").position(new LatLng(-11.22151, -74.889679)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		america.add(mapa.addMarker(new MarkerOptions().title("Canadá").position(new LatLng(57.797944, -101.703186)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		america.add(mapa.addMarker(new MarkerOptions().title("Argentina").position(new LatLng(-35.137879, -65.309601)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		america.add(mapa.addMarker(new MarkerOptions().title("Brasil").position(new LatLng(-8.494105, -53.180695)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		america.add(mapa.addMarker(new MarkerOptions().title("Panamá").position(new LatLng(8.798225, -80.333748)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		america.add(mapa.addMarker(new MarkerOptions().title("Chile").position(new LatLng(-26.313113, -70.055695)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		america.add(mapa.addMarker(new MarkerOptions().title("Colombia").position(new LatLng(4.083453, -73.703156)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));

		ArrayList<Marker> africa = new ArrayList<Marker>();
		africa.add(mapa.addMarker(new MarkerOptions().title("Marruecos").position(new LatLng(31.784217, -7.67189)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		africa.add(mapa.addMarker(new MarkerOptions().title("Egipto").position(new LatLng(27.293689, 30.139618)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		africa.add(mapa.addMarker(new MarkerOptions().title("Etiopía").position(new LatLng(9.232249, 39.543915)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		africa.add(mapa.addMarker(new MarkerOptions().title("Madagascar").position(new LatLng(-19.766704, 46.663055)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		africa.add(mapa.addMarker(new MarkerOptions().title("Sudáfrica").position(new LatLng(-29.458731, 25.041962)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		africa.add(mapa.addMarker(new MarkerOptions().title("Kenya").position(new LatLng(0.549308, 37.742672)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		africa.add(mapa.addMarker(new MarkerOptions().title("Namibia").position(new LatLng(-22.755921, 17.043915)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		africa.add(mapa.addMarker(new MarkerOptions().title("Nigeria").position(new LatLng(9.362353, 7.815399)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		africa.add(mapa.addMarker(new MarkerOptions().title("Bostwana").position(new LatLng(-22.22809, 23.767548)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		africa.add(mapa.addMarker(new MarkerOptions().title("Camerún").position(new LatLng(5.090944, 12.253876)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
				
		ArrayList<Marker> oceania = new ArrayList<Marker>();
		oceania.add(mapa.addMarker(new MarkerOptions().title("Australia").position(new LatLng(-24.766785, 134.458923)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		oceania.add(mapa.addMarker(new MarkerOptions().title("Nueva Zelanda").position(new LatLng(-43.261206, 171.285095)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		oceania.add(mapa.addMarker(new MarkerOptions().title("Papúa Nueva Guinea").position(new LatLng(-6.227934, 144.039001)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		oceania.add(mapa.addMarker(new MarkerOptions().title("Vanuatu").position(new LatLng(-15.390136, 166.913567)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		oceania.add(mapa.addMarker(new MarkerOptions().title("Samoa").position(new LatLng(-13.629972, -172.414799)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
		
		//en "marcadores" se guardan todos los markers agrupados por continentes
		marcadores = new SparseArray<ArrayList<Marker>>();
		marcadores.put(EUROPA, europa);
		marcadores.put(ASIA, asia);
		marcadores.put(AMERICA, america);
		marcadores.put(AFRICA, africa);
		marcadores.put(OCEANIA, oceania);	
		
		//en "marcadoresVisitados" se guardarán los markers correspondientes a los paises sobre los que se haya preguntado
		marcadoresVisitados = new SparseArray<ArrayList<Marker>>();
		marcadoresVisitados.put(EUROPA, new ArrayList<Marker>(europa.size()));
		marcadoresVisitados.put(ASIA, new ArrayList<Marker>(asia.size()));
		marcadoresVisitados.put(AMERICA, new ArrayList<Marker>(america.size()));
		marcadoresVisitados.put(AFRICA, new ArrayList<Marker>(africa.size()));
		marcadoresVisitados.put(OCEANIA, new ArrayList<Marker>(oceania.size()));
	}	
}
