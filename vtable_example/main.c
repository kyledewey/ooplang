#include "stdio.h"
#include "stdlib.h"

// class Object {}
// abstract class Operation extends Object {
//   int value;
//   Operation(int value) {
//     super();
//     this.value = value;
//   }
//   int getValue() { return value; }
//   abstract char* getName();
//   abstract int doOperation(int first, int second);
// }
// class Add extends Operation {
//   Add(int value) {
//     super(value);
//   }
//   char* getName() { return "add"; }
//   int doOperation(int x, int y) { return value + x + y; }
// }
// class Mult extends Operation {
//   Mult(int value) {
//     super(value);
//   }
//   char* getName() { return "mult"; }
//   int doOperation(int x, int y) { return value * x * y; }
// }
// int main(int argc, char** argv) {
//   Add add = new Add(2);
//   Operation mult = new Mult(3);
//   print(add.getValue());
//   print(add.getName());
//   print(add.doOperation(3, 4));
//   print(mult.getValue());
//   print(mult.getName());
//   print(mult.doOperation(5, 6));
// }

struct Object {
  void** _vtable;
};

struct Operation {
  struct Object super;
  int value;
};

struct Add {
  struct Operation super;
};

struct Mult {
  struct Operation super;
};

void _init_Object(struct Object* object);
struct Object* _new_Object();
void _init_Operation(struct Operation* operation,
                     int value);
void _init_Add(struct Add* add, int value);
struct Add* _new_Add(int value);
void _init_Mult(struct Mult* mult, int value);
struct Mult* _new_Mult(int value);
int _method_Operation_getValue(struct Operation* op);
char* _method_Add_getName(struct Add* add);
int _method_Add_doOperation_int_int(struct Add* add,
                            int first,
                            int second);
char* _method_Mult_getName(struct Mult* mult);
int _method_Mult_doOperation_int_int(struct Mult* mult,
                             int first,
                             int second);
int main(int argc, char** argv);

void* _vtable_Operation[] = {
  (void*)&_method_Operation_getValue,
  NULL,
  NULL
};
void* _vtable_Add[] = {
  (void*)&_method_Operation_getValue,
  (void*)&_method_Add_getName,
  (void*)&_method_Add_doOperation_int_int
};
void* _vtable_Mult[] = {
  (void*)&_method_Operation_getValue,
  (void*)&_method_Mult_getName,
  (void*)&_method_Mult_doOperation_int_int
};

typedef int (*_typedef_Operation_getValue)(struct Operation*);
typedef char* (*_typedef_Operation_getName)(struct Operation*);
typedef int (*_typedef_Operation_doOperation_int_int)(struct Operation*, int, int);

// init assumes that the vtable is already set
void _init_Object(struct Object* object) {}

struct Object* _new_Object() {
  struct Object* retval =
    (struct Object*)malloc(sizeof(struct Object));
  retval->_vtable = NULL;
  _init_Object(retval);
  return retval;
}

void _init_Operation(struct Operation* this,
                     int value) {
  _init_Object((struct Object*)this);
  this->value = value;
}

void _init_Add(struct Add* this, int value) {
  _init_Operation((struct Operation*)this, value);
}

struct Add* _new_Add(int value) {
  struct Add* retval =
    (struct Add*)malloc(sizeof(struct Add));
  ((struct Object*)retval)->_vtable = _vtable_Add;
  _init_Add(retval, value);
  return retval;
}

void _init_Mult(struct Mult* this, int value) {
  _init_Operation((struct Operation*)this, value);
}

struct Mult* _new_Mult(int value) {
  struct Mult* retval =
    (struct Mult*)malloc(sizeof(struct Mult));
  ((struct Object*)retval)->_vtable = _vtable_Mult;
  _init_Mult(retval, value);
  return retval;
}

int _method_Operation_getValue(struct Operation* this) {
  return this->value;
}

char* _method_Add_getName(struct Add* this) {
  return "add";
}

int _method_Add_doOperation_int_int(struct Add* this,
                                    int first,
                                    int second) {
  return ((struct Operation*)this)->value + first + second;
}

char* _method_Mult_getName(struct Mult* this) {
  return "mult";
}

int _method_Mult_doOperation_int_int(struct Mult* this,
                                     int first,
                                     int second) {
  return ((struct Operation*)this)->value * first * second;
}

// for each initial definition of a method, we also need a helper like this
// This avoids double-evaluation of this, because the expression needs this twice
int _virtual_Operation_doOperation_int_int(struct Operation* this,
                                           int first,
                                           int second) {
  return ((_typedef_Operation_doOperation_int_int)((struct Object*)this)->_vtable[2])(this, first, second);
}

int _virtual_Operation_getValue(struct Operation* this) {
  return ((_typedef_Operation_getValue)((struct Object*)this)->_vtable[0])(this);
}

char* _virtual_Operation_getName(struct Operation* this) {
  return ((_typedef_Operation_getName)((struct Object*)this)->_vtable[1])(this);
}
                                 

int main(int argc, char** argv) {
  // Add add = new Add(2);
  struct Add* add = _new_Add(2);
  // Operation mult = new Mult(3);
  struct Operation* mult = (struct Operation*)_new_Mult(3);
  // print(add.getValue());
  printf("%i\n", _virtual_Operation_getValue((struct Operation*)add));
  // print(add.getName());
  printf("%s\n", _virtual_Operation_getName((struct Operation*) add));
  // print(add.doOperation(3, 4));
  printf("%i\n", _virtual_Operation_doOperation_int_int((struct Operation*)add, 3, 4));
  // print(mult.getValue());
  printf("%i\n", _virtual_Operation_getValue((struct Operation*)mult));
  // print(mult.getName());
  printf("%s\n", _virtual_Operation_getName((struct Operation*)mult));
  // print(mult.doOperation(5, 6));
  printf("%i\n", _virtual_Operation_doOperation_int_int((struct Operation*) mult, 3, 4));
  return 0;
}

