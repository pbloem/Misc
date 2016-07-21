import theano as th
import theano.tensor as T

a = T.scalar('a')
b = T.scalar('b')

# c is the symbolic representation of 'a * b'
c = a * b
print(th.pp(c))

# cf is the compiled version of c ...
print('.')
cf = th.function([a, b], c)
print('.')
print(cf)

# ... which we can call like any other function
print(cf(3, 8))

# We can also make the function change the state of some external variable,
# called a _shared_ variable

i = th.shared(value=0.0, name='i')
cf = th.function([a, b], c, updates=[(i, i + 1)])

print('cf has been called {} times'.format(i.get_value()))

print(cf(3, 8))
print(cf(3, 8))
print(cf(3, 8))

print('cf has been called {} times '.format(i.get_value()))