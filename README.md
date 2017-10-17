# Pathfinding Visual Comparison #

This project provides a visual side-by-side comparison of several common
pathfinding algorithms.

![Pathfinding Comparison](img/header.png)

## Overview ##
The comparison window consists of several grids, each displaying the execution
of one algorithm. These grids can be edited by clicking and dragging on their contents,
with changes being synchronized across all grids.

Buttons at the bottom of the window can be used to play an animation of all algorithms
running, advance each algorithm a single step, or clear/reset the grids.

### Features include: ###

* Comparing multiple algorithms side-by-side in one window
* Grid editing: Placing obstacles and moving start and target points to create custom scenarios
* Weighted graphs: Adding a travel cost to certain cells
* Preset setups (seedable):
    * Mazes
    * Randomized grids
    * Weighted gradients
* Step-by-step viewing of algorithm procedure
* Adjustable run speed

### Included Algorithms: ###

* [Dijkstra's Algorithm](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm)
* [A* Search](https://en.wikipedia.org/wiki/A*_search_algorithm) (3 variants)
  * Regular A*
  * Higher heuristic weight
  * Heuristic with tiebreaker
* [Breadth First Search](https://en.wikipedia.org/wiki/Breadth-first_search)
* [Greedy Best First Search](https://en.wikipedia.org/wiki/Best-first_search#Greedy_BFS)

## Running ##

### Option 1: Run from .jar ###

Just run the precompiled [PathfindingComparison.jar](PathfindingComparison.jar).

### Option 2: Compile and run ###

Compile the contents of [src/](src) and run [Main](src/Main.java):
```
cd src
javac *.java
java Main
```

## Editing ##

Most settings can be changed by editing the class constants at the top of [Main.java](src/Main.java).
Additional pathfinders can be added by implementing the [Pathfinder](src/Pathfinder.java) interface and
then loading the pathfinder in the `main` method of [Main.java](src/Main.java) with:
```
loadedPathfinders.add(new MyPathfinder());
```

### Useful Editable Constants ###
* `GRID_WIDTH`, `GRID_HEIGHT` -- The size of the grids in cells.
* `CELL_SIZE` -- The size of each grid cell in pixels.
* `GRIDS_HORZ`, `GRIDS_VERT` -- The number of grids per row and per column in the window. `GRIDS_HORZ * GRIDS_VERT`
is the number of pathfinding algorithms that will be shown simultaneously.
* `MAX_COST` -- Determines the max weight a cell can have. Note that the "Toggle Cell Costs" button will only show
 the first digit of the weight (e.g. a weight of 15 will display as '1').
* `DEFAULT_STEP_DELAY` -- The default delay between steps when running.

## Presets ##

Several randomly generated presets are available. Examples of them are:

![Presets](img/presets.png)


## License ##
This project is licensed under the MIT license. See [LICENSE](LICENSE) for details.
