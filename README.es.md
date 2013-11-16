Presupuesto con sobres
=========================================
Retome el control de su dinero. Es fácil.
-----------------------------------------

Evite cargos por sobregiro, falta de dinero inesperada, el camino sin fin del pago mínimo, y ese sentimiento de ahogo cuando se da cuenta de que realmente no podía permitirse el lujo de salir.

Eso es lo que Presupuesto hace, y es fácil. Usted divide su dinero en los sobres al día de pago, y retira de ellos a medida que gasta. Un sobre puede representar cualquier cosa - una cuenta bancaria, dinero destinado a alimentos, o el dinero que está ahorrando para comprar ese nuevo teléfono Android fuera de contrato. Piense en una chequera, pero más flexible.

El manejo del dinero es un lastre, pero no tiene por qué ser doloroso o difícil.


¿Qué hay de nuevo en la versión 3.4?
====================================

* Corrección de errores.

* Añadir más colores.

* Traducción al ruso.


Compilación
===========

En resumen: Igual que cualquier otro proyecto para Android.


Versión de depuración:
----------------------

Para compilar en modo de depuración, ejecute lo siguiente la primera vez:

    android update project -p .

Y luego, todas las veces:

    ant clean
    ant debug


Versión de lanzamiento
----------------------

Compilar una versión de lanzamiento es un poco más difícil, porque se necesita una llave de lanzamiento. Hay un tutorial disponible sobre esto en <https://developer.android.com/tools/publishing/app-signing.html> (en inglés). A continuación puede ver un ejemplo para la generación de la llave (sólo es necesario hacerlo una vez):

    android update project -p . # A menos que ya lo haya hecho, por supuesto.
    keytool -genkey -keystore budget.keystore -alias budget -keyalg RSA -keysize 2048 -validity 10000
    echo "key.store=budget.keystore" > ant.properties
    echo "key.alias=budget" >> ant.properties

Y para compilar el programa:

    ant clean
    ant release


Dentro de un árbol de Android
-----------------------------

Si quiere distribuir Presupuesto con una ROM personalizada de Android, querrá compilar de esta manera. He aquí un `.repo/local_manifest.xml` de ejemplo:

    <?xml version="1.0" encoding="UTF-8"?>
    <manifest>
        <remote fetch="http://github.com/" name="gh" revision="master"/>
        <project name="notriddle/budget-envelopes" path="packages/apps/budget-envelopes" remote="gh" revision="release"/>
    </manifest>

Esto compilará una versión de lanzamiento de Presupuesto, firmada con su llave de plataforma. Si usted quiere ser capaz de instalar su propia compilación de Presupuesto *y* la de la tienda Google Play o F-Droid, puede definir `BUDGET_DEBUG` mientras compila:

    BUDGET_DEBUG=1 mm

