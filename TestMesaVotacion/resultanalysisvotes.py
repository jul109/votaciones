import os
import pandas as pd

# Carpeta de entrada
CARPETA_RESULTADOS = 'resultados'
ARCHIVOS = [
    'test_results_1800.csv',
    'test_results_3600.csv',
    'test_results_7200.csv'
]

# Archivo de salida
ARCHIVO_SALIDA = os.path.join(CARPETA_RESULTADOS, 'resumen_resultados_test_votacion.csv')

# Crear lista para guardar los resultados agregados
resumen_global = []

print("📊 Iniciando análisis de archivos de votación...\n")

for archivo in ARCHIVOS:
    ruta = os.path.join(CARPETA_RESULTADOS, archivo)
    print(f"▶ Procesando archivo: {ruta}")
    
    try:
        df = pd.read_csv(ruta)

        total = len(df)
        total_ms = df["tiempoMs"].sum()
        promedio = df["tiempoMs"].mean()
        minimo = df["tiempoMs"].min()
        maximo = df["tiempoMs"].max()
        throughput = round(total / (total_ms / 1000), 2) if total_ms > 0 else 0.0

        print(f"🧾 Total de votos         : {total}")
        print(f"⏱️ Tiempo total (ms)      : {total_ms}")
        print(f"📉 Tiempo promedio (ms)   : {promedio:.2f}")
        print(f"⏬ Tiempo mínimo (ms)     : {minimo}")
        print(f"⏫ Tiempo máximo (ms)     : {maximo}")
        print(f"📈 Throughput (votos/s)   : {throughput}\n")

        resumen_global.append({
            "archivo": archivo,
            "total_votos": total,
            "tiempo_total_ms": total_ms,
            "tiempo_promedio_ms": round(promedio, 2),
            "tiempo_minimo_ms": minimo,
            "tiempo_maximo_ms": maximo,
            "throughput_votos_s": throughput
        })

    except Exception as e:
        print(f"❌ Error procesando {archivo}: {e}\n")

# Guardar resumen en CSV
resumen_df = pd.DataFrame(resumen_global)
resumen_df.to_csv(ARCHIVO_SALIDA, index=False)
print(f"✅ Procesamiento completado. Resumen guardado en: {ARCHIVO_SALIDA}")
