# 5–Software Design — Project

Welcome! This repository contains the **starter setup** for the *5–Software Design* **project**.

You will work in **teams of 2 students** on **one** of the three project options described below. The non‑functional
requirements (design patterns, UML, tests, …) are **identical for all options**.

The powerpoint and BB are always right. If the readme differs from them, follow the PowerPoint.

---

## Project Overview

* Choose **exactly one** of the following:

    * **Option 1:** Smart Meal Planner & Grocery List Generator
    * **Option 2:** Board Game Tournament Manager
    * **Option 3:** Travel Planner & Trip Overview Manager
* Groups of **2 students**.
* When you have problems working in a team, contact the lecturer as soon as possible.
* Use everything you have learned during the labs (UML, design patterns, testing).

> Deadlines, submission details and the group registration form are announced on Blackboard.

---

## Non‑Functional Requirements (same for all project options)

### Design patterns

The following design patterns are **mandatory** to implement:

* **MVC**
* **Singleton** (thread‑safe)
* **Observer**
  (the Observer aspect inherent in MVC **does not** count for this)
* **One of** the following creational patterns:

    * Factory Method
    * Abstract Factory
    * Builder

In addition, you must implement **at least one** of the following patterns:

* Strategy
* Decorator
* Command
* Adapter
* Façade
* Proxy
* Composite
* State

> Every design pattern should be implemented where it **fits the domain** and is **logically motivated** —
> do not add patterns just to tick a box.

### UML diagrams

You must deliver the following UML diagrams for your chosen project:

* **Class diagrams**

    * **One class diagram of the whole application.**
      (The GUI can be abstracted to a single `GUI` or `View` class.)
    * **One mini class diagram for each design pattern** you implement.
      Each mini diagram should show only the classes and relationships that form the pattern.
* **Use case diagram** of the entire application

    * Show the **important actors**.
    * Include all **main use cases** corresponding to the functional requirements.
* **Sequence diagram**

    * At least **one sequence diagram** for a key use case in your application.

### Tests

* **Unit tests**

    * Full unit test coverage for **at least one important class**.
* **Integration tests**

    * At least **one integration test** that exercises a realistic use case (e.g. via controller + model).

> Tip: Create the UML first and keep it as a **live updated blueprint**.
>
> Tip: Write tests **as you go**, not only at the end.

---

## Functional Requirements

Below you find the functional requirements for each project option

---

## Option 1 – Smart Meal Planner & Grocery List Generator

### Manage recipes

* Each recipe has at least:

    * **Title**
    * **Description**
    * **Ingredient list**
    * **Optional tags** (e.g. *vegetarian*, *quick*, *budget*)
    * … (you may add more fields)
* The user can:

    * **Add** recipes
    * **View** recipes
    * **Edit** recipes
    * **Remove** recipes

### Manage weekly meal plan

* **7 days** (Mon – Sun)
* Per day, at least the following meal slots:

    * Breakfast
    * Lunch
    * Dinner
    * Snacks
    * Optional: let the user **configure** which meal slots exist
* The user can:

    * **Choose recipes for each day**
    * **View the plan**
    * **Change or remove** the planned recipe for a day

### Generate grocery list

* Based on the **current weekly meal plan**:

    * All ingredients from the planned recipes are collected.
    * **Quantities per ingredient are summed**
      (e.g. 2× `100 g pasta` → `200 g pasta`).
* The grocery list is **automatically generated and updated** when the plan changes.
* In the grocery list, the user can:

    * **Check off items** as bought
    * **Add extra items** manually

### Optional

* Actual **data persistence** across sessions (use a file or database).
* You are allowed to **add custom features**.

---

## Option 2 – Board Game Tournament Manager

### Manage players

* Each player has at least:

    * **Name**
    * **Skill level / rating**
    * **Description**
    * **Age**
    * … (you may add more fields)
* The user can:

    * **Add** players
    * **View** players
    * **Edit** players
    * **Remove** players

### Manage game types

* Each game type has at least:

    * **Name**
    * **Min/Max number of players per match**
    * **Scoring rule (points)**
    * **Description**
    * **Variant**
    * … (you may add more fields)
* There must be **at least a few predefined game types**, e.g.:

    * Java, Catan, Carcassonne, Chess, …
* The user can:

    * **Select**, **edit**, **create**, **view** and **delete** game types

### Create and manage tournaments

* A tournament has at least:

    * **Chosen game type**
    * **Number of rounds**
    * **List of registered players**
    * **Day of tournament**
    * **Description**
    * … (you may add more fields)
* The user can:

    * **Create**, **edit**, **delete**, **view** tournaments
    * **Add players** to a tournament
    * **View basic information** of a tournament

### Rounds, matches, and standings

* The user can:

    * **Generate pairings for each round**
      (the pairing strategy can be chosen by you)
    * View the **list of matches** for that round
    * Enter a **result for each match** (e.g. winner, draw, …)
    * **View results per round**
* The system must:

    * **Automatically calculate total points per player**
    * At the end of the tournament, show:

        * An **overall ranking**
        * **Intermediate standings** for each round

### Optional

* Actual **data persistence** across sessions (use a file or database).
* You are allowed to **add custom features**.

---

## Option 3 – Travel Planner & Trip Overview Manager

### Manage trips

* Each trip has at least:

    * **Title**
    * **Destination**
    * **Start/End date**
    * **Description**
    * … (you may add more fields)
* The user can:

    * **Create** trips
    * **View** trips
    * **Edit** trips
    * **Delete** trips

### Manage travellers / participants

* Each traveller has at least:

    * **Name**
    * **Contact info**
    * **Age**
    * **Nationality / passport info**
    * … (you may add more fields)
* The user can:

    * **Create**, **view**, **edit**, **delete** travellers
    * **Assign travellers** to one or more trips
    * **Edit travellers from a trip**
    * **Remove travellers from a trip**
    * **View travellers** participating in a certain trip

### Manage itinerary (schedule) per trip

* For each trip, you manage **itinerary items**. Each item has at least:

    * **Title**
    * **Date/time**
    * **Type** (transport, accommodation, activity, …)
    * **Optional location**
    * **Description**
    * **Price**
    * … (you may add more fields)
* The user can:

    * **Create**, **view**, **edit**, **delete** itinerary items for each trip
    * View a **day overview** for each day of the trip
* The system must:

    * **Automatically calculate / adjust the total price** for each trip

### Optional

* Actual **data persistence** across sessions (use a file or database).
* You are allowed to **add custom features** (e.g. packing list).

---

## Execution, Code Reuse & Tools

### Groups

* Work in **groups of 2 students**.
* Fill in the Blackboard form with the name of your teammate before the deadline announced in class / on Blackboard.

### Using code or diagrams of others

* **From fellow students:** discouraged

    * You will only be graded on what is **yours**, not on what is written by others.
    * Copying code or diagrams from other groups is considered **plagiarism**.
* **From the internet:**

    * You *may* reuse code or ideas, but **give credit**.
    * Add a short credits section (websites, names, YouTube links, StackOverflow, …) if you reuse external material.

### Git & GitHub

* Try to use **Git** and optionally **GitHub**.
* Git is ideal for group projects, with features such as branching and committing.
* It is also for your **own safety** (backup, history).
* If you have never used Git before, you can start with an introductory video, e.g.:

    * [https://www.youtube.com/watch?v=SWYqp7iY_Tc](https://www.youtube.com/watch?v=SWYqp7iY_Tc)

### AI usage

* You are allowed to use **generative AI**, but:

    * You must keep a **logbook** describing **why, when, and where** you used AI.
    * AI may only be used to deepen your understanding of design patterns and to assist in debugging.
    * You must fully **understand all code and diagrams** you submit.
* **Warning:** AI can hallucinate and be wrong. Always be critical and never blindly copy/paste the output.

---

Good luck, and have fun designing and implementing your project!
