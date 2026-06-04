package com.tillzo.pos.domain.base

/**
 * Base UseCase contract.
 * Every UseCase in domain/ implements this interface.
 *
 * I  = Input parameter type (use Unit if no input needed)
 * O  = Output type wrapped in Result<O> for error handling
 *
 * Architecture Law: UseCases only call Repositories — never DAOs directly.
 */
interface BaseUseCase<in I, out O> {
    suspend operator fun invoke(input: I): O
}
