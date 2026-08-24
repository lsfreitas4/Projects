module Main where
import System.IO
import Operations
import StringToFormat
import FormatToString

--função main onde atua o menu UI com o utilizador
main::IO()
main = do
    putStrLn "Escolha uma opcao:"
    putStrLn "1- Normalizar um polinómio"
    putStrLn "2- Adicionar polinómios"
    putStrLn "3- Multiplicar polinómios"
    putStrLn "4- Derivar polinómios"
    putStrLn "5- Extras"
    putStrLn "0- Quit"
    opcao <- getLine
    case opcao of
               "1" -> normalizeSubMenu
               "2" -> somaSubMenu
               "3" -> multSubMenu
               "4" -> derivadaSubMenu
               "5" -> extraMenu
               "0" -> error "You pressed quit!"

--sub menu da parte de normalizar o polinómio, em que pede o polinómio e dá o resultado normalizado
normalizeSubMenu::IO()
normalizeSubMenu = do
  putStrLn "Insira o polinómio em string:"
  poli<-getLine
  putStrLn ("\nRESULTADO: "++ Operations.strNormalizeString poli++"\n")
  main

--sub menu da soma, em que se pedem 2 polinómios em string e se dá o resultado da soma em string
somaSubMenu::IO()
somaSubMenu = do
  putStrLn "Insira o 1º polinómio em string:"
  poli<-getLine
  putStrLn "Insira o 2º polinómio em string:"
  poli1<-getLine
  putStrLn ("\nRESULTADO: "++ Operations.strSomaPolsString poli poli1++"\n")
  main

--sub menu da multiplicação, em que se apresentam as opções de multiplicação de um polinómio por constante ou multiplicação de 2 polinómios
multSubMenu::IO()
multSubMenu=do
            putStrLn "1- Multiplicar por uma constante"
            putStrLn "2- Multiplicar 2 polinómios"
            opcao<-getLine
            case opcao of
              "1" -> multConst
              "2" -> mult2

--sub menu da multiplicação por constante, onde se dá o resultado de multiplicar um polinómio por uma constante
multConst::IO()
multConst= do
  putStrLn "Escolha a constante:"
  vari<-getLine
  putStrLn "Insira o polinómio em string:"
  poli1<-getLine
  putStrLn ("\nRESULTADO: "++ Operations.strMultiplicateByConstantString poli1 (read vari :: Int)++"\n")
  main

--sub menu da multiplicação de 2 polinómios, onde se dá o resultado em string dessa operação
mult2::IO()
mult2 = do
  putStrLn "Insira um polinómio em string:"
  poli<-getLine
  putStrLn "Insira outro polinómio em string:"
  poli1<-getLine
  putStrLn ("\nRESULTADO: "++ Operations.strMultPNString poli poli1++"\n")
  main

--sub menu da derivada, onde se oferece a possibilidade de primeira derivada ou segunda derivada
derivadaSubMenu::IO()
derivadaSubMenu=do
            putStrLn "1- Primeira derivada"
            putStrLn "2- Segunda derivada"
            opcao<-getLine
            case opcao of
              "1" -> pDerivada
              "2" -> sDerivada

--sub menu da primeira derivada, onde se pede a variável a derivar e o polinómio em string, dando-se depois o resultado em string
pDerivada::IO()
pDerivada = do
         putStrLn "Escolha a variável a derivar:"
         vari<-getLine --getChar não estava a funcionar
         putStrLn "Insira o polinómio em string:"
         poli1<-getLine
         putStrLn ("\nRESULTADO: "++ Operations.strDerivePolynomialString (head(vari)) poli1++"\n")
         main

--sub menu da segunda derivada, onde se pede a variável a derivar e o polinómio em string, dando-se depois o resultado em string
sDerivada::IO()
sDerivada = do
          putStrLn "Escolha a variável a derivar:"
          vari<-getLine --getChar não estava a funcionar
          putStrLn "Insira o polinómio em string:"
          poli1<-getLine
          putStrLn ("\nRESULTADO: "++ Operations.strSecondDerivePolynomialString (head(vari)) poli1++"\n")
          main

--sub menu das features extra, onde se apresentam as opções de grau do polinómio ou conversão de string para FR
extraMenu::IO()
extraMenu = do
     putStrLn "1- Grau do polinómio"
     putStrLn "2- String -> Forma representativa"
     opcao<-getLine
     case opcao of
       "1" -> grau
       "2" -> transformacao

--sub menu do grau do polinómio, onde se pede um polinómio em string e se dá o seu grau num inteiro
grau::IO()
grau = do
  putStrLn "Insira o polinómio em string:"
  poli<-getLine
  let poli1=StringToFormat.strToFr poli
  let graupoli=Operations.polynomialDegree poli1
  putStrLn ("\nGRAU: "++show (graupoli)++"\n")
  main

--sub menu da conversão de um polinómio de string para FR, onde se dá o resultado na FR
transformacao::IO()
transformacao = do
  putStrLn "Insira o polinómio em string:"
  poli<-getLine
  let poli1= Operations.normalize (StringToFormat.strToFr poli)
  putStrLn ("\nPOLINÓMIO EM FR: "++show (poli1)++"\n")
  main
