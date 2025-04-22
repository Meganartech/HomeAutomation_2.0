import React, { useState } from 'react';

export default function Register() {
    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        mobile: '',
        password: '',
        confirmPassword: ''
    });

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();
    };

    return (
        <>
            <div className="container my-4">
                <div className="card shadow border-0 p-4 mx-auto" style={{ maxWidth: '500px' }}>
                    <h3 className="mb-4">Register</h3>
                    <form onSubmit={handleSubmit}>
                        <div className="form-floating mb-3">
                            <input type="text" name="fullName" className="form-control mb-1" placeholder='' value={formData.fullName} onChange={handleChange} required />
                            <label className='text-6c757d'>Full Name</label>
                        </div>
                        <div className="form-floating mb-3">
                            <input type="email" name="email" className="form-control mb-1" placeholder='' value={formData.email} onChange={handleChange} required />
                            <label className='text-6c757d'>Email</label>
                        </div>
                        <div className="form-floating mb-3">
                            <input type="tel" name="mobile" className="form-control mb-1" placeholder='' value={formData.mobile} onChange={handleChange} required />
                            <label className='text-6c757d'>Mobile Number</label>
                        </div>
                        <div className="form-floating mb-3">
                            <input type="password" name="password" className="form-control mb-1" placeholder='' value={formData.password} onChange={handleChange} required />
                            <label className='text-6c757d'>Password</label>
                        </div>
                        <div className="form-floating mb-4">
                            <input type="password" name="confirmPassword" className="form-control mb-1" placeholder='' value={formData.confirmPassword} onChange={handleChange} required />
                            <label className='text-6c757d'>Confirm Password</label>
                        </div>
                        <div className='d-flex justify-content-center mb-3'>
                            <button type="submit" className="btn btn-dark w-75">Register</button>
                        </div>
                    </form>
                    <p className="text-center text-muted" style={{ fontSize: '0.9rem' }}>
                        By registering you agree to <strong>Terms & Conditions</strong> and <br />
                        <strong>Privacy Policy</strong> of the Vermo
                    </p>
                </div>
            </div>
        </>
    );
};