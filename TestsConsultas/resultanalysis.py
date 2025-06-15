import pandas as pd
import glob
import os

from time import time

def procesar_resultados(path_glob='resultados/results-*.csv', salida='resultados/resumen_resultados.csv'):
    csv_files = glob.glob(path_glob)
    resumen = []

    if os.path.exists(salida):
        os.remove(salida)

    for archivo in csv_files:
        print(f"\n▶ Procesando archivo: {archivo}")
        try:
            df = pd.read_csv(
                archivo,
                header=None,
                names=["Dispositivo", "Documento", "Tiempo_Respuesta(ms)", "Respuesta"],
                usecols=[0, 1, 2, 3],
                encoding="utf-8",
                quotechar='"',
                on_bad_lines='skip',
                low_memory=False
            )

            df["Tiempo_Respuesta(ms)"] = pd.to_numeric(df["Tiempo_Respuesta(ms)"], errors='coerce')
            df = df.dropna(subset=["Tiempo_Respuesta(ms)"])

            total_consultas = len(df)
            print(f"📊 Total de consultas: {total_consultas}")

            total_tiempo = df["Tiempo_Respuesta(ms)"].sum()
            print(f"⏱️ Tiempo total (ms): {total_tiempo:.1f}")

            tiempo_promedio = df["Tiempo_Respuesta(ms)"].mean()
            print(f"📏 Tiempo promedio (ms): {tiempo_promedio:.2f}")

            tiempo_minimo = df["Tiempo_Respuesta(ms)"].min()
            print(f"🟦 Tiempo mínimo (ms): {tiempo_minimo}")

            tiempo_maximo = df["Tiempo_Respuesta(ms)"].max()
            print(f"🟥 Tiempo máximo (ms): {tiempo_maximo}")

            consultas_por_segundo = total_consultas / (total_tiempo / 1000) if total_tiempo > 0 else 0
            print(f"⚡ Consultas por segundo: {consultas_por_segundo:.2f}")

            fila = {
                "Archivo": os.path.basename(archivo),
                "Total Consultas": total_consultas,
                "Tiempo Total (ms)": round(total_tiempo, 2),
                "Tiempo Promedio (ms)": round(tiempo_promedio, 2),
                "Tiempo Mínimo (ms)": tiempo_minimo,
                "Tiempo Máximo (ms)": tiempo_maximo,
                "Consultas por Segundo": round(consultas_por_segundo, 2)
            }

            resumen.append(fila)

            # Guardado incremental
            pd.DataFrame([fila]).to_csv(salida, mode='a', index=False, header=not os.path.exists(salida))

        except Exception as e:
            print(f"❌ Error procesando {archivo}: {e}")

    return pd.DataFrame(resumen)


if __name__ == "__main__":
    resumen_df = procesar_resultados()
    print("\n✅ Procesamiento finalizado. Resumen incremental guardado en: resultados/resumen_resultados.csv")
