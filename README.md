Разработать инструмент командной строки для визуализации графа
зависимостей, включая транзитивные зависимости. Сторонние средства для
получения зависимостей использовать нельзя.
Зависимости определяются для git-репозитория. Для описания графа
зависимостей используется представление PlantUML. Визуализатор должен
выводить результат на экран в виде графического изображения графа.
120
Построить граф зависимостей для коммитов, в узлах которого находятся
списки файлов и папок.
Ключами командной строки задаются:
• Путь к программе для визуализации графов.
• Путь к анализируемому репозиторию.
Все функции визуализатора зависимостей должны быть покрыты тестами.
