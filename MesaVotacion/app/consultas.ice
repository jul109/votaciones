module consultaVotacion {

    sequence<string> CiudadanosList;

    interface queryStation{
       string query(string document);
       CiudadanosList obtenerCiudadanos(int mesaId);
    };
}; 